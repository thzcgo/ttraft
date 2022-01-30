package com.thzc.ttraft.core.node;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class NodeImpl implements Node {

    private final NodeContext context;
    private boolean started; // 是否启动
    private AbstractNodeRole role; // 当前的角色
    private StateMachine stateMachine;

    // callback for async tasks.
    private static final FutureCallback<Object> LOGGING_FUTURE_CALLBACK = new FutureCallback<Object>() {
        @Override
        public void onSuccess(@Nullable Object result) {
        }

        @Override
        public void onFailure(@Nonnull Throwable t) {

        }
    };

    public NodeImpl(NodeContext context) {
        this.context = context;
    }

    @Override
    public void registerStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
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
    public void appendLog(byte[] commandBytes) {

    }

    private ElectionTimeout scheduleElectionTimeout() {
        return context.getScheduler().schedulerElectionTimeout(this::electionTimeout);
    }


    // 角色变更
    private void changeToRole(AbstractNodeRole newRole) {
        NodeStore store = context.getStore();
        store.setTerm(newRole.getTerm());
        if (newRole.getName() == RoleName.FOLLOWER) {
            store.setVotedFor(((FollowerNodeRole)newRole).getVotedFor());
        }
        role = newRole;
    }

    @Override
    public void stop() throws InterruptedException {
        if (!started) throw new IllegalStateException("节点未启动");

        context.getScheduler().stop();
        context.getConnector().close();
        context.getTaskExecutor().shutdown();

        started = false;
    }

    @Override
    @Nonnull
    public RoleNameAndLeaderId getRoleNameAndLeaderId() {
        return role.getNameAndLeaderId(context.getSelfId());
    }

    // 选举超时
    private void electionTimeout() {
        context.getTaskExecutor().submit(this::doProcessElectionTimeout);
    }

    private void doProcessElectionTimeout() {
        if (role.getName() == RoleName.LEADER) return;
        int newTerm = role.getTerm() + 1;
        role.cancelTimeoutOrTask();
        // follower -> candidate
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

    // 接收RequestVote消息
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

    private void changeToFollower(int term, NodeId votedFor, NodeId leaderId, boolean scheduleElectionTimeout) {
        role.cancelTimeoutOrTask();
        if (leaderId != null && !leaderId.equals(context.getSelfId())) {
            //
        }
        ElectionTimeout electionTimeout = scheduleElectionTimeout ? scheduleElectionTimeout() : ElectionTimeout.NONE;
        changeToRole(new FollowerNodeRole(term, votedFor, leaderId, electionTimeout));
    }

    // candidaate收到RequestVote响应
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
        if (role.getName() != RoleName.CANDIDATE) return;
        if (result.getTerm() < role.getTerm() || !result.isVoteGranted()) return;

        int currentVotesCount = ((CandidateNodeRole)role).getVotesCount() + 1;
        int countOfMajor = context.getGroup().getCount();

        role.cancelTimeoutOrTask();
        if (currentVotesCount > countOfMajor / 2) {
            changeToRole(new LeaderNodeRole(role.getTerm(), scheduleLogReplicationTask()));
        } else {
            changeToRole(new CandidateNodeRole(role.getTerm(), currentVotesCount, scheduleElectionTimeout()));
        }
    }

    private LogReplicationTask scheduleLogReplicationTask() {
        return context.getScheduler().schedulerLogReplicationTask(this::replicateLog);
    }


    // leader 发送心跳信息
    void replicateLog() {
        context.getTaskExecutor().submit(this::doReplicateLog, LOGGING_FUTURE_CALLBACK);
    }



    private void doReplicateLog() {
        for (GroupMember member : context.getGroup().listReplicationTarget()) {
            doReplicateLog(member);
        }
    }

    private void doReplicateLog(GroupMember member) {
        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setTerm(role.getTerm());
        rpc.setLeaderId(context.getSelfId());
        rpc.setPrevLogIndex(0);
        rpc.setPrevLogTerm(0);
        rpc.setLeaderCommit(0);
        context.getConnector().sendAppendEntries(rpc,member.getEndpoint());
    }

    private void doReplicateLog(GroupMember member, int maxEntries) {
        AppendEntriesRpc rpc = context.getLog().createAppendEntriesRpc(role.getTerm(), context.getSelfId(), member.getNextIndex(), maxEntries);
        context.getConnector().sendAppendEntries(rpc,member.getEndpoint());
    }

    // 非leader节点收到心跳信息
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
                return new AppendEntriesResult(rpc.getTerm(), false);
            default:
                throw new IllegalStateException("节点名异常");
        }
    }

    private boolean appendEntries(AppendEntriesRpc rpc) {
        boolean b = context.getLog().appednEntriesFromLeader(rpc.getPrevLogIndex(), rpc.getPrevLogTerm(), rpc.getEntries());
        if (b) {
            context.getLog().advanceCommitIndex(Math.min(rpc.getLeaderCommit(), rpc.getLastEntryIndex()), rpc.getTerm());
        }
        return b;
    }

    // leader 节点收到心跳回应
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
            //
            return;
        }

        NodeId sourceId = resultMessage.getSourceNodeId();
        GroupMember member = context.getGroup().getMember(sourceId);
        if (member == null) return;
        AppendEntriesRpc rpc = resultMessage.getAppendEntriesRpc();
        if (result.isSuccess()) {
            if (member.advanceReplicationState(rpc.getLastEntryIndex())) {
                context.getLog().advanceCommitIndex(context.getGroup().getMatchIndexOfMajor(), role.getTerm());
            } else {
                if (!member.backOffNextIndex()) {

                }
            }
        }
    }


}
