package com.thzc.ttraft.core.node.role;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LogReplicationTask {

    private final ScheduledFuture<?> scheduledFuture;

    public LogReplicationTask(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public void cancel() {
        this.scheduledFuture.cancel(false);
    }

    @Override
    public String toString() {
        return "选举超时：" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + "ms 后进行";
    }
}
