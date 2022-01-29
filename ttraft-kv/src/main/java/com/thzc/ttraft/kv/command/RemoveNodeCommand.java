package com.thzc.ttraft.kv.command;


import com.thzc.ttraft.core.node.role.NodeId;

public class RemoveNodeCommand {

    private final NodeId nodeId;

    public RemoveNodeCommand(String nodeId) {
        this.nodeId = new NodeId(nodeId);
    }

    public NodeId getNodeId() {
        return nodeId;
    }

}
