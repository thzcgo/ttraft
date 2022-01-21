package com.thzc.ttraft.core.node.role;

public class AppendEntriesResultMessage {

    private AppendEntriesRpc appendEntriesRpc;
    private AppendEntriesResult appendEntriesResult;

    private final NodeId sourceNodeId;

    public AppendEntriesResultMessage(AppendEntriesRpc appendEntriesRpc, AppendEntriesResult appendEntriesResult, NodeId sourceNodeId) {
        this.appendEntriesRpc = appendEntriesRpc;
        this.appendEntriesResult = appendEntriesResult;
        this.sourceNodeId = sourceNodeId;
    }

    public AppendEntriesRpc getAppendEntriesRpc() {
        return appendEntriesRpc;
    }

    public void setAppendEntriesRpc(AppendEntriesRpc appendEntriesRpc) {
        this.appendEntriesRpc = appendEntriesRpc;
    }

    public AppendEntriesResult getAppendEntriesResult() {
        return appendEntriesResult;
    }

    public void setAppendEntriesResult(AppendEntriesResult appendEntriesResult) {
        this.appendEntriesResult = appendEntriesResult;
    }

    public NodeId getSourceNodeId() {
        return sourceNodeId;
    }
}
