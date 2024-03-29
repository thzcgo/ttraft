package com.thzc.ttraft.core.rpc.message;

import com.thzc.ttraft.core.node.NodeId;

/*
*  candidate 发送给 follower 请求投票
* */
public class RequestVoteRpc {
    private int term;
    private NodeId candidateId;
    private int lastLogIndex = 0;
    private int lastLogTerm = 0;

    public int getTerm() {
        return term;
    }

    public NodeId getCandidateId() {
        return candidateId;
    }

    public int getLastLogIndex() {
        return lastLogIndex;
    }

    public int getLastLogTerm() {
        return lastLogTerm;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public void setCandidateId(NodeId candidateId) {
        this.candidateId = candidateId;
    }

    public void setLastLogIndex(int lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public void setLastLogTerm(int lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }

    @Override
    public String toString() {
        return "RequestVote{" +
                "term=" + term +
                ", candidateId=" + candidateId +
                ", lastLogIndex=" + lastLogIndex +
                ", lastLogTerm=" + lastLogTerm +
                '}';
    }
}
