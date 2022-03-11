package com.thzc.ttraft.core.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ElectionTimeout {

    private static final Logger logger = LoggerFactory.getLogger(ElectionTimeout.class);

    private final ScheduledFuture<?> scheduledFuture;

    public ElectionTimeout(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public void cancel() {
        logger.debug("cancel election timeout");
        this.scheduledFuture.cancel(false);
    }

    @Override
    public String toString() {
        if (scheduledFuture.isCancelled()) return "选举超时：已取消";
        if (scheduledFuture.isDone()) return "选举超时已完成";
        return "选举超时：" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + "ms 后进行";
    }
}
