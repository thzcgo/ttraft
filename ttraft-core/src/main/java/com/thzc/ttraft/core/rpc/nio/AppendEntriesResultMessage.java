package com.thzc.ttraft.core.rpc.nio;

import com.google.common.base.Preconditions;
import com.thzc.ttraft.core.node.role.AppendEntriesResult;
import com.thzc.ttraft.core.node.role.AppendEntriesRpc;
import com.thzc.ttraft.core.node.role.NodeId;

import javax.annotation.Nonnull;

public class AppendEntriesResultMessage {

    private final AppendEntriesResult result;
    private final NodeId sourceNodeId;
    // TODO remove rpc, just lastEntryIndex required, or move to replicating state?
    private final AppendEntriesRpc rpc;

    public AppendEntriesResultMessage(AppendEntriesResult result, NodeId sourceNodeId, @Nonnull AppendEntriesRpc rpc) {
        Preconditions.checkNotNull(rpc);
        this.result = result;
        this.sourceNodeId = sourceNodeId;
        this.rpc = rpc;
    }

    public AppendEntriesResult get() {
        return result;
    }

    public NodeId getSourceNodeId() {
        return sourceNodeId;
    }

    public AppendEntriesRpc getRpc() {
        return rpc;
    }

}
