package com.thzc.ttraft.core.rpc.message;

public class AppendEntriesResult {

    private final String rpcMessageId;
    private final int term;
    private final boolean success; // 是否追加成功

    public AppendEntriesResult(String rpcMessageId, int term, boolean success) {
        this.rpcMessageId = rpcMessageId;
        this.term = term;
        this.success = success;
    }

    public String getRpcMessageId() {
        return rpcMessageId;
    }

    public int getTerm() {
        return term;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "AppendEntriesResult{" +
                "rpcMessageId='" + rpcMessageId + '\'' +
                ", success=" + success +
                ", term=" + term +
                '}';
    }
}
