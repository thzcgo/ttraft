package com.thzc.ttraft.core.node.role;

public class GroupMember {

    private final NodeEndpoint endpoint;
    private ReplicatingState replicatingState;

    public GroupMember(NodeEndpoint endpoint, ReplicatingState replicatingState) {
        this.endpoint = endpoint;
        this.replicatingState = replicatingState;
    }

    public GroupMember(NodeEndpoint endpoint) {
        this(endpoint, null);
    }

    public NodeEndpoint getEndpoint() {
        return endpoint;
    }

    public NodeId getId() {
        return endpoint.getId();
    }

    public ReplicatingState getReplicatingState() {
        return replicatingState;
    }

    public void setReplicatingState(ReplicatingState replicatingState) {
        this.replicatingState = replicatingState;
    }

    private ReplicatingState ensureReplicatingState() {
        if (replicatingState == null) throw new IllegalStateException("复制进度未设置");
        return replicatingState;
    }

    int getNextIndex() {
        return ensureReplicatingState().getNextIndex();
    }

    int getMatchIndex() {
        return ensureReplicatingState().getMatchIndex();
    }

    boolean idEquals(NodeId id) {
        return endpoint.getId().equals(id);
    }

    boolean advanceReplicationState(int lastEntryIndex) {
        return ensureReplicatingState().advance(lastEntryIndex);
    }

    boolean backOffNextIndex() {
        return ensureReplicatingState().backOffNextIndex();
    }
}
