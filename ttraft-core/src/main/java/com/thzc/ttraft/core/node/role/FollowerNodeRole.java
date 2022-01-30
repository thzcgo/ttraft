package com.thzc.ttraft.core.node.role;

import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.schedule.ElectionTimeout;

public class FollowerNodeRole extends AbstractNodeRole{

    private final NodeId votedFor; // 投过票的节点，可能为空
    private final NodeId leaderId; // 当前leader节点Id，可能为空
    private final ElectionTimeout electionTimeout; // 选举超时


    public FollowerNodeRole(int term, NodeId votedFor, NodeId leaderId, ElectionTimeout electionTimeout) {
        super(RoleName.FOLLOWER, term);
        this.votedFor = votedFor;
        this.leaderId = leaderId;
        this.electionTimeout = electionTimeout;
    }

    @Override
    public void cancelTimeoutOrTask() {
        electionTimeout.cancel();
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        return leaderId;
    }

    public NodeId getVotedFor() {
        return votedFor;
    }

    public NodeId getLeaderId() {
        return leaderId;
    }

    @Override
    public String toString() {
        return "FollowerNodeRole{" +
                "term=" + term +
                ", votedFor=" + votedFor +
                ", leaderId=" + leaderId +
                ", electionTimeout=" + electionTimeout +
                '}';
    }
}
