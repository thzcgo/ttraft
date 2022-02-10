package com.thzc.ttraft.core.node;

class ReplicatingState {

    private int nextIndex;
    private int matchIndex;
    private boolean replicating = false;
    private long lastReplicatedAt = 0;

    ReplicatingState(int nextIndex) {
        this(nextIndex, 0);
    }

    ReplicatingState(int nextIndex, int matchIndex) {
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
    }

    boolean backOffNextIndex() {
        if (nextIndex > 1) {
            nextIndex--;
            return true;
        }
        return false;
    }

    boolean advance(int lastEntryIndex) {
        // changed
        boolean result = (matchIndex != lastEntryIndex || nextIndex != (lastEntryIndex + 1));
        matchIndex = lastEntryIndex;
        nextIndex = lastEntryIndex + 1;
        return result;
    }

    int getNextIndex() {
        return nextIndex;
    }

    int getMatchIndex() {
        return matchIndex;
    }

    long getLastReplicatedAt() {
        return lastReplicatedAt;
    }

    boolean isReplicating() {
        return replicating;
    }

    void setReplicating(boolean replicating) {
        this.replicating = replicating;
    }

    void setLastReplicatedAt(long lastReplicatedAt) {
        this.lastReplicatedAt = lastReplicatedAt;
    }

    @Override
    public String toString() {
        return "ReplicatingState{" +
                "nextIndex=" + nextIndex +
                ", matchIndex=" + matchIndex +
                ", replicating=" + replicating +
                ", lastReplicatedAt=" + lastReplicatedAt +
                '}';
    }

}
