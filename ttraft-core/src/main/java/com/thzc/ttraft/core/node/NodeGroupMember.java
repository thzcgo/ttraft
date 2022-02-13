package com.thzc.ttraft.core.node;


class NodeGroupMember {

    private final NodeEndpoint endpoint;
    private ReplicatingState replicatingState;
    private boolean major;
    private boolean removing = false;

    NodeGroupMember(NodeEndpoint endpoint) {
        this(endpoint, null, true);
    }

    NodeGroupMember(NodeEndpoint endpoint, ReplicatingState replicatingState, boolean major) {
        this.endpoint = endpoint;
        this.replicatingState = replicatingState;
        this.major = major;
    }

    NodeEndpoint getEndpoint() {
        return endpoint;
    }

    NodeId getId() {
        return endpoint.getId();
    }

    boolean idEquals(NodeId id) {
        return endpoint.getId().equals(id);
    }

    void setReplicatingState(ReplicatingState replicatingState) {
        this.replicatingState = replicatingState;
    }

    boolean isReplicationStateSet() {
        return replicatingState != null;
    }

    private ReplicatingState ensureReplicatingState() {
        if (replicatingState == null) {
            throw new IllegalStateException("replication state not set");
        }
        return replicatingState;
    }

    boolean isMajor() {
        return major;
    }

    void setMajor(boolean major) {
        this.major = major;
    }

    boolean isRemoving() {
        return removing;
    }

    void setRemoving() {
        removing = true;
    }

    int getNextIndex() {
        return ensureReplicatingState().getNextIndex();
    }

    int getMatchIndex() {
        return ensureReplicatingState().getMatchIndex();
    }

    boolean advanceReplicatingState(int lastEntryIndex) {
        return ensureReplicatingState().advance(lastEntryIndex);
    }

    boolean backOffNextIndex() {
        return ensureReplicatingState().backOffNextIndex();
    }

    void replicateNow() {
        replicateAt(System.currentTimeMillis());
    }

    void replicateAt(long replicatedAt) {
        ReplicatingState replicatingState = ensureReplicatingState();
        replicatingState.setReplicating(true);
        replicatingState.setLastReplicatedAt(replicatedAt);
    }

    boolean isReplicating() {
        return ensureReplicatingState().isReplicating();
    }

    void stopReplicating() {
        ensureReplicatingState().setReplicating(false);
    }


    boolean shouldReplicate(long readTimeout) {
        ReplicatingState replicatingState = ensureReplicatingState();
        return !replicatingState.isReplicating() ||
                System.currentTimeMillis() - replicatingState.getLastReplicatedAt() >= readTimeout;
    }

    @Override
    public String toString() {
        return "GroupMember{" +
                "endpoint=" + endpoint +
                ", major=" + major +
                ", removing=" + removing +
                ", replicatingState=" + replicatingState +
                '}';
    }

}
