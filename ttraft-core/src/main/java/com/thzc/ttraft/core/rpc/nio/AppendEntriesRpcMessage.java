package com.thzc.ttraft.core.rpc.nio;


import com.thzc.ttraft.core.node.role.AppendEntriesRpc;
import com.thzc.ttraft.core.node.role.NodeId;

public class AppendEntriesRpcMessage extends AbstractRpcMessage<AppendEntriesRpc> {

    public AppendEntriesRpcMessage(AppendEntriesRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }

}
