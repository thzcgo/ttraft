package com.thzc.ttraft.core.node.role;

// follower 响应给 candidate 的投票情况
public class RequestVoteResult {

    private int term;
    private final boolean voteGranted; // 是给candidate投票

    public RequestVoteResult(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public int getTerm() {
        return term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    @Override
    public String toString() {
        return "RequestVoteResult{" +
                "term=" + term +
                ", voteGranted=" + voteGranted +
                '}';
    }
}
