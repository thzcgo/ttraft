package com.thzc.ttraft.kv.server.command;


import com.thzc.ttraft.core.node.NodeId;

public class RemoveNodeCommand {

    private final NodeId nodeId;

    public RemoveNodeCommand(String nodeId) {
        this.nodeId = new NodeId(nodeId);
    }

    public NodeId getNodeId() {
        return nodeId;
    }

}
