package com.thzc.ttraft.core.node.role;

public class AppendEntriesResultMessage {

    private AppendEntriesRpc appendEntriesRpc;
    private AppendEntriesResult appendEntriesResult;

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
}
