package com.thzc.ttraft.core.node.role;

public class LeaderNodeRole extends AbstractNodeRole{

    private final LogReplicationTask logReplicationTask;

    public LeaderNodeRole(int term, LogReplicationTask logReplicationTask) {
        super(RoleName.LEADER, term);
        this.logReplicationTask = logReplicationTask;
    }


    @Override
    public void cancelTimeoutOrTask() {
        logReplicationTask.cancel();
    }

    @Override
    public String toString() {
        return "LeaderNodeRole{" +
                "term=" + term +
                ", logReplicationTask=" + logReplicationTask +
                '}';
    }
}
