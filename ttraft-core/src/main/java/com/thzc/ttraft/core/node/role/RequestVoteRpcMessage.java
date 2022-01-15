package com.thzc.ttraft.core.node.role;

public class RequestVoteRpcMessage {

    private RequestVoteRpc rpc;
    private NodeId sourceNodeId;

    public RequestVoteRpc getRpc() {
        return rpc;
    }

    public void setRpc(RequestVoteRpc rpc) {
        this.rpc = rpc;
    }

    public NodeId getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(NodeId sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }
}
