package com.thzc.ttraft.core.node.role;

import javax.annotation.Nonnull;

public class NodeMatchIndex implements Comparable<NodeMatchIndex>{

    private final NodeId nodeId;
    private final int matchIndex;

    public NodeMatchIndex(NodeId nodeId, int matchIndex) {
        this.nodeId = nodeId;
        this.matchIndex = matchIndex;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    @Override
    public int compareTo(@Nonnull NodeMatchIndex o) {
        return Integer.compare(this.matchIndex, o.matchIndex);
    }

    @Override
    public String toString() {
        return "NodeMatchIndex{" +
                "nodeId=" + nodeId +
                ", matchIndex=" + matchIndex +
                '}';
    }
}
