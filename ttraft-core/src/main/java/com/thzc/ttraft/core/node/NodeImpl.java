package com.thzc.ttraft.core.node;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.FutureCallback;
import com.thzc.ttraft.core.log.entry.EntryMeta;
import com.thzc.ttraft.core.log.statemachine.StateMachine;
import com.thzc.ttraft.core.node.role.*;
import com.thzc.ttraft.core.node.store.NodeStore;
import com.thzc.ttraft.core.rpc.message.*;
import com.thzc.ttraft.core.rpc.message.RequestVoteRpcMessage;
import com.thzc.ttraft.core.schedule.ElectionTimeout;
import com.thzc.ttraft.core.schedule.LogReplicationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class NodeImpl implements Node {

    private static final Logger logger = LoggerFactory.getLogger(NodeImpl.class);

    private AbstractNodeRole role; // 当前的角色
    private final NodeContext context;
    private boolean started; // 是否启动
    private StateMachine stateMachine;

    // callback for async tasks.
    public NodeImpl(NodeContext context) {
        this.context = context;
    }

    @Override
    public synchronized void start() {
        if (started) return;
        context.getEventBus().register(this);
        context.getConnector().initialize();
        NodeStore store = context.getStore();
        changeToRole(new FollowerNodeRole(store.getTerm(), store.getVotedFor(), null, scheduleElectionTimeout()));
        started = true;
    }

    @Override
    public void stop() throws InterruptedException {
        if (!started) throw new IllegalStateException("节点未启动");
        context.getScheduler().stop();
        context.getConnector().close();
        context.getTaskExecutor().shutdown();
        started = false;
    }


    /*********************************  角色变更  *********************************************************/
    private void changeToRole(AbstractNodeRole newRole) {
        NodeStore store = context.getStore();
        store.setTerm(newRole.getTerm());
        if (newRole.getName() == RoleName.FOLLOWER) {
            store.setVotedFor(((FollowerNodeRole)newRole).getVotedFor());
        }
        logger.debug("node {}, role state changed -> {}", context.getSelfId(), newRole);
        role = newRole;
    }

    private void changeToFollower(int term, NodeId votedFor, NodeId leaderId, boolean scheduleElectionTimeout) {
        role.cancelTimeoutOrTask();
        if (leaderId != null && !leaderId.equals(context.getSelfId())) {
            logger.info("current leader is {}, term {}", leaderId, term);
        }
        ElectionTimeout electionTimeout = scheduleElectionTimeout ? scheduleElectionTimeout() : ElectionTimeout.NONE;
        changeToRole(new FollowerNodeRole(term, votedFor, leaderId, electionTimeout));
    }

    /************  选举超时，当前节点从follower 变成 candidate，向其他follower发送 RequestVote 消息  *************/
    /*
    *  选举超时，当前节点从follower 变成 candidate，向其他节点follower发送 RequestVote 消息
    *
    * */
    private ElectionTimeout scheduleElectionTimeout() {
        return context.getScheduler().schedulerElectionTimeout(this::electionTimeout);
    }

    private void electionTimeout() {
        context.getTaskExecutor().submit(this::doProcessElectionTimeout);
    }

    private void doProcessElectionTimeout() {
        if (role.getName() == RoleName.LEADER) {
            logger.warn("node {}, current role is leader, ignore election timeout", context.getSelfId());
            return;
        }
        int newTerm = role.getTerm() + 1;
        role.cancelTimeoutOrTask();

        // 判断是否为单机模式
        if (context.getGroup().isStandalone()) {
            if (context.getMode() == NodeMode.STANDBY) {
                logger.info("starts with standby mode, skip election");
            } else {

                // become leader
                logger.info("become leader, term {}", newTerm);
                resetReplicatingStates();
                changeToRole(new LeaderNodeRole(newTerm, scheduleLogReplicationTask()));
                context.getLog().appendEntry(newTerm); // no-op log
            }
        } else {
            // follower -> candidate
            logger.info("start election");
            changeToRole(new CandidateNodeRole(newTerm, scheduleElectionTimeout()));
            EntryMeta lastEntryMeta = context.getLog().getLastEntryMeta();
            // 发送 RequestVote
            RequestVoteRpc rpc = new RequestVoteRpc();
            rpc.setTerm(newTerm);
            rpc.setCandidateId(context.getSelfId());
            rpc.setLastLogIndex(lastEntryMeta.getIndex());
            rpc.setLastLogTerm(lastEntryMeta.getTerm());
            context.getConnector().sendRequestVote(rpc, context.getGroup().listEndpointExceptSelf());
        }
    }

    private void resetReplicatingStates() {
        context.getGroup().resetReplicatingStates(context.getLog().getNextIndex());
    }

    /******************************* 接收RequestVote消息   **********************************************/
    /*
    *  当前节点为 follower，接收到 RequestVote 消息处理后回复 RequestVoteResult 消息
    *
    * */
    @Subscribe
    public void onReceiveRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
        context.getTaskExecutor().submit(
                () -> context.getConnector().replyRequestVote(doProcessRequestVoteRpc(rpcMessage), rpcMessage),
                LOGGING_FUTURE_CALLBACK
        );
    }

    private RequestVoteResult doProcessRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
        RequestVoteRpc rpc = rpcMessage.getRpc();
        if (rpc.getTerm() < role.getTerm()) {
            logger.debug("term from rpc < current term, don't vote ({} < {})", rpc.getTerm(), role.getTerm());
            return new RequestVoteResult(role.getTerm(), false);
        }

        boolean voteForCandidate = !context.getLog().isNewerThen(rpc.getLastLogIndex(), rpc.getLastLogTerm());

        if (rpc.getTerm() > role.getTerm()) {
            return new RequestVoteResult(rpc.getTerm(), voteForCandidate);
        }

        switch (role.getName()) {
            case LEADER:
                return new RequestVoteResult(role.getTerm(), false);
            case CANDIDATE:
            case FOLLOWER:
                FollowerNodeRole followerNodeRole = (FollowerNodeRole) role;
                NodeId voteFor = followerNodeRole.getVotedFor();
                // 以下需要投票
                if ((voteFor == null && voteForCandidate)  // 自己未投票。且对方日志更新
                        || Objects.equals(voteFor, rpc.getCandidateId()) // 已投过票，需要切换为follower
                ) {
                    changeToFollower(role.getTerm(), rpc.getCandidateId(), null, true);
                    return new RequestVoteResult(rpc.getTerm(), true);
                }
                return new RequestVoteResult(role.getTerm(), false);
            default:
                throw new IllegalStateException("节点角色异常");
        }
    }

    /******************************* 收到RequestVoteResult响应  **********************************************/
    /*
    *  当前节点为 candidate，收到 RequestVoteResult 响应，根据票数开始角色变化
    * */
    @Subscribe
    public void onReceiveRequestVoteResult(RequestVoteResult result) {
        context.getTaskExecutor().submit(
                () -> doProcessRequestVoteResult(result)
        );
    }

    private void doProcessRequestVoteResult(RequestVoteResult result) {

        // 退化成 follower
        if (result.getTerm() > role.getTerm()) {
            changeToFollower(result.getTerm(), null, null, true);
            return;
        }
        if (role.getName() != RoleName.CANDIDATE) {
            logger.debug("receive request vote result and current role is not candidate, ignore");
            return;
        }
        if (result.getTerm() < role.getTerm() || !result.isVoteGranted()) return;

        int currentVotesCount = ((CandidateNodeRole)role).getVotesCount() + 1;
        int countOfMajor = context.getGroup().getCount();
        logger.debug("votes count {}, major node count {}", currentVotesCount, countOfMajor);
        role.cancelTimeoutOrTask();
        if (currentVotesCount > countOfMajor / 2) {
            logger.info("become leader, term {}", role.getTerm());
            changeToRole(new LeaderNodeRole(role.getTerm(), scheduleLogReplicationTask()));
        } else {
            changeToRole(new CandidateNodeRole(role.getTerm(), currentVotesCount, scheduleElectionTimeout()));
        }
    }


    /*******************************  日志复制任务  **********************************************/
    /*
    *  当前节点为leader，给其他节点发送 AppendEntriesRpc 心跳消息
    * */
    private LogReplicationTask scheduleLogReplicationTask() {
        return context.getScheduler().schedulerLogReplicationTask(this::replicateLog);
    }

    // leader 发送心跳信息
    void replicateLog() {
        context.getTaskExecutor().submit(this::doReplicateLog, LOGGING_FUTURE_CALLBACK);
    }

    private void doReplicateLog() {
        // 单机模式下不需要日志复制，但是要推进commitIndex
        if (context.getGroup().isStandalone()) {
            context.getLog().advanceCommitIndex(context.getLog().getNextIndex() - 1, role.getTerm());
            return;
        }
        logger.debug("replicate log");
        for (NodeGroupMember member : context.getGroup().listReplicationTarget()) {
            doReplicateLog(member);
        }
    }

    private void doReplicateLog(NodeGroupMember member) {
        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setTerm(role.getTerm());
        rpc.setLeaderId(context.getSelfId());
        rpc.setPrevLogIndex(0);
        rpc.setPrevLogTerm(0);
        rpc.setLeaderCommit(0);
        context.getConnector().sendAppendEntries(rpc,member.getEndpoint());
    }

    private void doReplicateLog(NodeGroupMember member, int maxEntries) {
        AppendEntriesRpc rpc = context.getLog().createAppendEntriesRpc(role.getTerm(), context.getSelfId(), member.getNextIndex(), maxEntries);
        context.getConnector().sendAppendEntries(rpc,member.getEndpoint());
    }

    /*******************************  非leader节点收到心跳信息  **********************************************/
    /*
    *  非leader节点收到 AppendEntriesRpc 心跳信息，处理后，回复 AppendEntriesResult 消息
    *
    * */
    @Subscribe
    public void onReceiveAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
        context.getTaskExecutor().submit(() ->
                        context.getConnector().replyAppendEntries(doProcessAppendEntriesRpc(rpcMessage), rpcMessage),
                LOGGING_FUTURE_CALLBACK
        );
    }

    private AppendEntriesResult doProcessAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
        AppendEntriesRpc rpc = rpcMessage.getRpc();
        if (rpc.getTerm() < role.getTerm()) {
            return new AppendEntriesResult(role.getTerm(), false);
        }
        if (rpc.getTerm() > role.getTerm()) {
            changeToFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
            return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc));
        }
        switch (role.getName()) {
            case FOLLOWER:
                changeToFollower(rpc.getTerm(),((FollowerNodeRole)role).getVotedFor(), rpc.getLeaderId(), true);
                return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc));
            case CANDIDATE:
                changeToFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
                return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc));
            case LEADER:
                logger.warn("receive append entries rpc from another leader {}, ignore", rpc.getLeaderId());
                return new AppendEntriesResult(rpc.getTerm(), false);
            default:
                throw new IllegalStateException("节点名异常");
        }
    }

    private boolean appendEntries(AppendEntriesRpc rpc) {
        boolean b = context.getLog().appendEntriesFromLeader(rpc.getPrevLogIndex(), rpc.getPrevLogTerm(), rpc.getEntries());
        if (b) {
            context.getLog().advanceCommitIndex(Math.min(rpc.getLeaderCommit(), rpc.getLastEntryIndex()), rpc.getTerm());
        }
        return b;
    }


    /*******************************  leader 节点收到心跳回应  **********************************************/
    /*
    *   leader 节点收到 AppendEntriesResult 消息，开始处理
    * */
    @Subscribe
    public void onReceiveAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        context.getTaskExecutor().submit(
                () -> doProcessAppendEntriesResult(resultMessage)
        );
    }

    private void doProcessAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        AppendEntriesResult result = resultMessage.getAppendEntriesResult();
        if (result.getTerm() > role.getTerm()) {
            changeToFollower(result.getTerm(), null, null, true);
            return;
        }
        if (role.getName() != RoleName.LEADER) {
            logger.warn("receive append entries result from node {} but current node is not leader, ignore", resultMessage.getSourceNodeId());
            return;
        }

        NodeId sourceId = resultMessage.getSourceNodeId();
        NodeGroupMember member = context.getGroup().getMember(sourceId);
        if (member == null) {
            logger.info("unexpected append entries result from node {}, node maybe removed", sourceId);
            return;
        }
        AppendEntriesRpc rpc = resultMessage.getAppendEntriesRpc();
        if (result.isSuccess()) {
            if (member.advanceReplicationState(rpc.getLastEntryIndex())) {
                context.getLog().advanceCommitIndex(context.getGroup().getMatchIndexOfMajor(), role.getTerm());
            } else {
                if (!member.backOffNextIndex()) {
                    logger.warn("cannot back off next index more, node {}", sourceId);
                }
            }
        }
    }

    /*******  追加日志  ***********************************************************/
    @Override
    public void appendLog(@Nonnull byte[] commandBytes) {
        Preconditions.checkNotNull(commandBytes);
        ensureLeader();
        context.getTaskExecutor().submit(() -> {
            context.getLog().appendEntry(role.getTerm(), commandBytes);
            doReplicateLog();
        }, LOGGING_FUTURE_CALLBACK);
    }

    private void ensureLeader() {
        RoleNameAndLeaderId result = role.getNameAndLeaderId(context.getSelfId());
        if (result.getRoleName() == RoleName.LEADER) {
            return;
        }
        NodeEndpoint endpoint = result.getLeaderId() != null ? context.getGroup().findMember(result.getLeaderId()).getEndpoint() : null;
        throw new NotLeaderException(result.getRoleName(), endpoint);
    }


    /*******************************  其它  **********************************************/

    private static final FutureCallback<Object> LOGGING_FUTURE_CALLBACK = new FutureCallback<Object>() {
        @Override
        public void onSuccess(@Nullable Object result) {
        }

        @Override
        public void onFailure(@Nonnull Throwable t) {
            logger.warn("failure", t);
        }
    };

    @Override
    public synchronized void registerStateMachine(@Nonnull StateMachine stateMachine) {
        Preconditions.checkNotNull(stateMachine);
        context.getLog().setStateMachine(stateMachine);
    }



    @Override
    @Nonnull
    public RoleNameAndLeaderId getRoleNameAndLeaderId() {
        return role.getNameAndLeaderId(context.getSelfId());
    }

}
