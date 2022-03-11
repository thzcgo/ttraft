package com.thzc.ttraft.core.node;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.DeadEvent;
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
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;
import java.util.concurrent.Future;

@ThreadSafe
public class NodeImpl implements Node {

    private static final Logger logger = LoggerFactory.getLogger(NodeImpl.class);

    private final NodeContext context;
    @GuardedBy("this")
    private boolean started;
    private volatile AbstractNodeRole role;

    /*
    *  用NodeContext构造节点
    * */
    NodeImpl(NodeContext context) {
        this.context = context;
    }

    NodeContext getContext() {
        return context;
    }

    @Override
    public synchronized void start() {
        if (started) {
            return;
        }
        context.getEventBus().register(this);
        context.getConnector().initialize();
        // load term, votedFor from store and become follower
        NodeStore store = context.getStore();
        changeToRole(new FollowerNodeRole(store.getTerm(), store.getVotedFor(), null, scheduleElectionTimeout()));
        started = true;
    }

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

    @Override
    public void appendLog(@Nonnull byte[] commandBytes) {
        Preconditions.checkNotNull(commandBytes);
        ensureLeader();
        context.getTaskExecutor().submit(() -> {
            context.getLog().appendEntry(role.getTerm(), commandBytes);
            doReplicateLog();
        });
    }


    /**
     * Ensure leader status
     *
     * @throws NotLeaderException if not leader
     */
    private void ensureLeader() {
        RoleNameAndLeaderId result = role.getNameAndLeaderId(context.getSelfId());
        if (result.getRoleName() == RoleName.LEADER) {
            return;
        }
        NodeEndpoint endpoint = result.getLeaderId() != null ? context.getGroup().findMember(result.getLeaderId()).getEndpoint() : null;
        throw new NotLeaderException(result.getRoleName(), endpoint);
    }



    private void becomeFollower(int term, NodeId votedFor, NodeId leaderId) {
        role.cancelTimeoutOrTask();
        if (leaderId != null && !leaderId.equals(role.getLeaderId(context.getSelfId()))) {
            logger.info("current leader is {}, term {}", leaderId, term);
        }
        ElectionTimeout electionTimeout = scheduleElectionTimeout();
        changeToRole(new FollowerNodeRole(term, votedFor, leaderId, electionTimeout));
    }


    private void changeToRole(AbstractNodeRole newRole) {
        if (!isStableBetween(role, newRole)) {
            logger.debug("node {}, role state changed -> {}", context.getSelfId(), newRole);
            RoleState state = newRole.getState();

            // update store
            NodeStore store = context.getStore();
            store.setTerm(state.getTerm());
            store.setVotedFor(state.getVotedFor());
        }
        role = newRole;
    }

    /**
     * Check if stable between two roles.
     * <p>
     * It is stable when role name not changed and role state except timeout/task not change.
     * </p>
     * <p>
     * If role state except timeout/task not changed, it should not update store or notify listeners.
     * </p>
     *
     * @param before role before
     * @param after  role after
     * @return true if stable, otherwise false
     * @see AbstractNodeRole#stateEquals(AbstractNodeRole)
     */
    private boolean isStableBetween(AbstractNodeRole before, AbstractNodeRole after) {
        assert after != null;
        return before != null && before.stateEquals(after);
    }

    /*****************  选举超时部分   *****************************************************************************/
    /**
     * 节点建立初，转成follower时开始进行选举超时任务
     * @return
     */
    private ElectionTimeout scheduleElectionTimeout() {
        return context.getScheduler().scheduleElectionTimeout(this::electionTimeout);
    }

    void electionTimeout() {
        context.getTaskExecutor().submit(this::doProcessElectionTimeout);
    }

    private void doProcessElectionTimeout() {
        if (role.getName() == RoleName.LEADER) {
            logger.warn("node {}, current role is leader, ignore election timeout", context.getSelfId());
            return;
        }

        // follower: start election
        // candidate: restart election
        int newTerm = role.getTerm() + 1;
        role.cancelTimeoutOrTask();

        if (context.getGroup().isStandalone()) {  // 单机模式下不需要选举超时了，也就无需发送 RequestVoteRpc
            if (context.getMode() == NodeMode.STANDBY) {
                logger.info("starts with standby mode, skip election");
            } else {

                // become leader
                logger.info("become leader, term {}", newTerm);
                resetReplicatingStates();
                changeToRole(new LeaderNodeRole(newTerm, scheduleLogReplicationTask()));  // 启动 日志复制任务
                context.getLog().appendEntry(newTerm); // no-op log
            }
        } else {
            logger.info("start election");
            changeToRole(new CandidateNodeRole(newTerm, scheduleElectionTimeout()));

            // request vote
            EntryMeta lastEntryMeta = context.getLog().getLastEntryMeta();
            RequestVoteRpc rpc = new RequestVoteRpc();
            rpc.setTerm(newTerm);
            rpc.setCandidateId(context.getSelfId());
            rpc.setLastLogIndex(lastEntryMeta.getIndex());
            rpc.setLastLogTerm(lastEntryMeta.getTerm());
            context.getConnector().sendRequestVote(rpc, context.getGroup().listEndpointOfMajorExceptSelf());
        }
    }

    /**
     * Reset replicating states.
     */
    private void resetReplicatingStates() {
        context.getGroup().resetReplicatingStates(context.getLog().getNextIndex());
    }
    /*****************  选举超时部分 结束   *****************************************************************************/




    /*****************  日志复制部分 开始   *****************************************************************************/
    private LogReplicationTask scheduleLogReplicationTask() {
        return context.getScheduler().scheduleLogReplicationTask(this::replicateLog);
    }


    void replicateLog() {
        context.getTaskExecutor().submit(this::doReplicateLog);
    }


    private void doReplicateLog() {
        // just advance commit index if is unique node
        if (context.getGroup().isStandalone()) {
            context.getLog().advanceCommitIndex(context.getLog().getNextIndex() - 1, role.getTerm());
            return;
        }
        logger.debug("replicate log");
        for (NodeGroupMember member : context.getGroup().listReplicationTarget()) {
            if (member.shouldReplicate(context.getConfig().getLogReplicationReadTimeout())) {
                doReplicateLog0(member, context.getConfig().getMaxReplicationEntries());
            } else {
                logger.debug("node {} is replicating, skip replication task", member.getId());
            }
        }
    }

    private void doReplicateLog0(NodeGroupMember member, int maxEntries) {
        member.replicateNow();
        AppendEntriesRpc rpc = context.getLog().createAppendEntriesRpc(role.getTerm(), context.getSelfId(), member.getNextIndex(), maxEntries);
        context.getConnector().sendAppendEntries(rpc, member.getEndpoint());
    }
    /*****************  日志复制部分 结束   *****************************************************************************/




    /***********  节点收到RequestVoteRpc消息   ********************************************************************/
    @Subscribe
    public void onReceiveRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
        context.getTaskExecutor().submit(
                () -> context.getConnector().replyRequestVote(doProcessRequestVoteRpc(rpcMessage), rpcMessage)
        );
    }

    private RequestVoteResult doProcessRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {

        // skip non-major node, it maybe removed node
        if (!context.getGroup().isMemberOfMajor(rpcMessage.getSourceNodeId())) {
            logger.warn("receive request vote rpc from node {} which is not major node, ignore", rpcMessage.getSourceNodeId());
            return new RequestVoteResult(role.getTerm(), false);
        }

        // reply current term if result's term is smaller than current one
        RequestVoteRpc rpc = rpcMessage.getRpc();
        if (rpc.getTerm() < role.getTerm()) {
            logger.debug("term from rpc < current term, don't vote ({} < {})", rpc.getTerm(), role.getTerm());
            return new RequestVoteResult(role.getTerm(), false);
        }

        // step down if result's term is larger than current term
        if (rpc.getTerm() > role.getTerm()) {
            boolean voteForCandidate = !context.getLog().isNewerThan(rpc.getLastLogIndex(), rpc.getLastLogTerm());
            becomeFollower(rpc.getTerm(), (voteForCandidate ? rpc.getCandidateId() : null), null);
            return new RequestVoteResult(rpc.getTerm(), voteForCandidate);
        }

        assert rpc.getTerm() == role.getTerm();
        switch (role.getName()) {
            case FOLLOWER:
                FollowerNodeRole follower = (FollowerNodeRole) role;
                NodeId votedFor = follower.getVotedFor();
                // reply vote granted for
                // 1. not voted and candidate's log is newer than self
                // 2. voted for candidate
                if ((votedFor == null && !context.getLog().isNewerThan(rpc.getLastLogIndex(), rpc.getLastLogTerm())) ||
                        Objects.equals(votedFor, rpc.getCandidateId())) {
                    becomeFollower(role.getTerm(), rpc.getCandidateId(), null);
                    return new RequestVoteResult(rpc.getTerm(), true);
                }
                return new RequestVoteResult(role.getTerm(), false);
            case CANDIDATE: // voted for self
            case LEADER:
                return new RequestVoteResult(role.getTerm(), false);
            default:
                throw new IllegalStateException("unexpected node role [" + role.getName() + "]");
        }
    }

    /***********  节点收到 RequestVoteResult 消息   ******************************************************************/
    @Subscribe
    public void onReceiveRequestVoteResult(RequestVoteResult result) {
        context.getTaskExecutor().submit(() -> doProcessRequestVoteResult(result));
    }

    Future<?> processRequestVoteResult(RequestVoteResult result) {
        return context.getTaskExecutor().submit(() -> doProcessRequestVoteResult(result));
    }

    private void doProcessRequestVoteResult(RequestVoteResult result) {

        // step down if result's term is larger than current term
        if (result.getTerm() > role.getTerm()) {
            becomeFollower(result.getTerm(), null, null);
            return;
        }

        // check role
        if (role.getName() != RoleName.CANDIDATE) {
            logger.debug("receive request vote result and current role is not candidate, ignore");
            return;
        }

        // do nothing if not vote granted
        if (!result.isVoteGranted()) {
            return;
        }

        int currentVotesCount = ((CandidateNodeRole) role).getVotesCount() + 1;
        int countOfMajor = context.getGroup().getCountOfMajor();
        logger.debug("votes count {}, major node count {}", currentVotesCount, countOfMajor);
        role.cancelTimeoutOrTask();
        if (currentVotesCount > countOfMajor / 2) {

            // become leader
            logger.info("become leader, term {}", role.getTerm());
            resetReplicatingStates();
            changeToRole(new LeaderNodeRole(role.getTerm(), scheduleLogReplicationTask()));
            context.getLog().appendEntry(role.getTerm()); // no-op log
            context.getConnector().resetChannels(); // close all inbound channels
        } else {

            // update votes count
            changeToRole(new CandidateNodeRole(role.getTerm(), currentVotesCount, scheduleElectionTimeout()));
        }
    }


    /***********  节点收到 AppendEntriesRpc 消息   ******************************************************************/
    @Subscribe
    public void onReceiveAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
        context.getTaskExecutor().submit(() ->
                        context.getConnector().replyAppendEntries(doProcessAppendEntriesRpc(rpcMessage), rpcMessage)
        );
    }

    private AppendEntriesResult doProcessAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
        AppendEntriesRpc rpc = rpcMessage.getRpc();

        // reply current term if term in rpc is smaller than current term
        if (rpc.getTerm() < role.getTerm()) {
            return new AppendEntriesResult(rpc.getMessageId(), role.getTerm(), false);
        }

        // if term in rpc is larger than current term, step down and append entries
        if (rpc.getTerm() > role.getTerm()) {
            becomeFollower(rpc.getTerm(), null, rpc.getLeaderId());
            return new AppendEntriesResult(rpc.getMessageId(), rpc.getTerm(), appendEntries(rpc));
        }

        assert rpc.getTerm() == role.getTerm();
        switch (role.getName()) {
            case FOLLOWER:

                // reset election timeout and append entries
                becomeFollower(rpc.getTerm(), ((FollowerNodeRole) role).getVotedFor(), rpc.getLeaderId());
                return new AppendEntriesResult(rpc.getMessageId(), rpc.getTerm(), appendEntries(rpc));
            case CANDIDATE:

                // more than one candidate but another node won the election
                becomeFollower(rpc.getTerm(), null, rpc.getLeaderId());
                return new AppendEntriesResult(rpc.getMessageId(), rpc.getTerm(), appendEntries(rpc));
            case LEADER:
                logger.warn("receive append entries rpc from another leader {}, ignore", rpc.getLeaderId());
                return new AppendEntriesResult(rpc.getMessageId(), rpc.getTerm(), false);
            default:
                throw new IllegalStateException("unexpected node role [" + role.getName() + "]");
        }
    }

    /**
     * Append entries and advance commit index if possible.
     *
     * @param rpc rpc
     * @return {@code true} if log appended, {@code false} if previous log check failed, etc
     */
    private boolean appendEntries(AppendEntriesRpc rpc) {
        boolean result = context.getLog().appendEntriesFromLeader(rpc.getPrevLogIndex(), rpc.getPrevLogTerm(), rpc.getEntries());
        if (result) {
            context.getLog().advanceCommitIndex(Math.min(rpc.getLeaderCommit(), rpc.getLastEntryIndex()), rpc.getTerm());
        }
        return result;
    }

    /***********  节点收到 AppendEntriesResult 消息   ******************************************************************/
    @Subscribe
    public void onReceiveAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        context.getTaskExecutor().submit(() -> doProcessAppendEntriesResult(resultMessage));
    }

    Future<?> processAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        return context.getTaskExecutor().submit(() -> doProcessAppendEntriesResult(resultMessage));
    }

    private void doProcessAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        AppendEntriesResult result = resultMessage.getAppendEntriesResult();

        // step down if result's term is larger than current term
        if (result.getTerm() > role.getTerm()) {
            becomeFollower(result.getTerm(), null, null);
            return;
        }

        // check role
        if (role.getName() != RoleName.LEADER) {
            logger.warn("receive append entries result from node {} but current node is not leader, ignore", resultMessage.getSourceNodeId());
            return;
        }

//        // dispatch to new node catch up task by node id
//        if (newNodeCatchUpTaskGroup.onReceiveAppendEntriesResult(resultMessage, context.getLog().getNextIndex())) {
//            return;
//        }

        NodeId sourceNodeId = resultMessage.getSourceNodeId();
        NodeGroupMember member = context.getGroup().getMember(sourceNodeId);
        if (member == null) {
            logger.info("unexpected append entries result from node {}, node maybe removed", sourceNodeId);
            return;
        }

        AppendEntriesRpc rpc = resultMessage.getAppendEntriesRpc();
        if (true) {
//        if (result.isSuccess()) {
            if (!member.isMajor()) {  // removing node
                if (member.isRemoving()) {
                    logger.debug("node {} is removing, skip", sourceNodeId);
                } else {
                    logger.warn("unexpected append entries result from node {}, not major and not removing", sourceNodeId);
                }
                member.stopReplicating();
                return;
            }

            // peer
            // advance commit index if major of match index changed
            if (member.advanceReplicatingState(rpc.getLastEntryIndex())) {
                context.getLog().advanceCommitIndex(context.getGroup().getMatchIndexOfMajor(), role.getTerm());
            }

            // node caught up
            if (member.getNextIndex() >= context.getLog().getNextIndex()) {
                member.stopReplicating();
                return;
            }
        } else {

            // backoff next index if failed to append entries
            if (!member.backOffNextIndex()) {
                logger.warn("cannot back off next index more, node {}", sourceNodeId);
                member.stopReplicating();
                return;
            }
        }

        // replicate log to node immediately other than wait for next log replication
        doReplicateLog0(member, context.getConfig().getMaxReplicationEntries());
    }





    @Override
    public synchronized void stop() throws InterruptedException {
        if (!started) {
            throw new IllegalStateException("node not started");
        }
        context.getScheduler().stop();
        context.getLog().close();
        context.getConnector().close();
        context.getStore().close();
        context.getTaskExecutor().shutdown();
        started = false;
    }



}
