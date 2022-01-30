package com.thzc.ttraft.core.rpc.message;


import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.rpc.Channel;

public class AppendEntriesRpcMessage extends AbstractRpcMessage<AppendEntriesRpc> {

    public AppendEntriesRpcMessage(AppendEntriesRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }

}
