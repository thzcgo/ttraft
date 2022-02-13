package com.thzc.ttraft.core.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LogReplicationTask {

    private static final Logger logger = LoggerFactory.getLogger(LogReplicationTask.class);

    private final ScheduledFuture<?> scheduledFuture;

    public LogReplicationTask(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public void cancel() {
        logger.debug("cancel log replication task");
        this.scheduledFuture.cancel(false);
    }

    @Override
    public String toString() {
        return "选举超时：" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + "ms 后进行";
    }
}
