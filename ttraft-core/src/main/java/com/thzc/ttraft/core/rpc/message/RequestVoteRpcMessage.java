package com.thzc.ttraft.core.rpc.message;

import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.rpc.Channel;
import com.thzc.ttraft.core.rpc.message.AbstractRpcMessage;
import com.thzc.ttraft.core.rpc.message.RequestVoteRpc;

public class RequestVoteRpcMessage extends AbstractRpcMessage<RequestVoteRpc> {

    public RequestVoteRpcMessage(RequestVoteRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }

}
