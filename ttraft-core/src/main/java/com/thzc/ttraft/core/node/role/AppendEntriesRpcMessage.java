package com.thzc.ttraft.core.node.role;

public class AppendEntriesRpcMessage {

    private AppendEntriesRpc rpc;
    private NodeId sourceNodeId;

    public AppendEntriesRpc getRpc() {
        return rpc;
    }

    public void setRpc(AppendEntriesRpc rpc) {
        this.rpc = rpc;
    }

    public NodeId getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(NodeId sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }
}
