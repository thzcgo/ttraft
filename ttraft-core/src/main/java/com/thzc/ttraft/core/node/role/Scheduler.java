package com.thzc.ttraft.core.node.role;

public interface Scheduler {

    LogReplicationTask schedulerLogReplicationTask(Runnable task);

    ElectionTimeout schedulerElectionTimeout(Runnable task);

    void stop() throws InterruptedException;
}
