package com.thzc.ttraft.core.node.role;

import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.schedule.ElectionTimeout;

public class CandidateNodeRole extends AbstractNodeRole{

    private final int votesCount; // 已得的票数
    private final ElectionTimeout electionTimeout; // 选举超时

    // 节点从follower变成candidate 时用
    public CandidateNodeRole(int term, ElectionTimeout electionTimeout) {
        this(term, 1, electionTimeout);
    }

    // 节点收到其他节点投票时用
    public CandidateNodeRole(int term, int votesCount, ElectionTimeout electionTimeout) {
        super(RoleName.CANDIDATE, term);
        this.votesCount = votesCount;
        this.electionTimeout = electionTimeout;
    }

    @Override
    public void cancelTimeoutOrTask() {
        electionTimeout.cancel();
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        return null;
    }


    public int getVotesCount() {
        return votesCount;
    }

    @Override
    public String toString() {
        return "CandidateNodeRole{" +
                "term=" + term +
                ", votesCount=" + votesCount +
                ", electionTimeout=" + electionTimeout +
                '}';
    }
}
