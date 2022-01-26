package com.thzc.ttraft.core.rpc.nio;

import com.thzc.ttraft.core.node.role.NodeId;
import com.thzc.ttraft.core.node.role.RequestVoteRpc;

public class RequestVoteRpcMessage extends AbstractRpcMessage<RequestVoteRpc> {

    public RequestVoteRpcMessage(RequestVoteRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }

}
