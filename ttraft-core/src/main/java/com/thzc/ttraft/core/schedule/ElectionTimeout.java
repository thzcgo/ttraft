package com.thzc.ttraft.core.schedule;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ElectionTimeout {

    public static final ElectionTimeout NONE = new ElectionTimeout(new NullScheduledFuture());
    private final ScheduledFuture<?> scheduledFuture;

    public ElectionTimeout(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public void cancel() {
        this.scheduledFuture.cancel(false);
    }

    @Override
    public String toString() {
        if (scheduledFuture.isCancelled()) return "选举超时：已取消";
        if (scheduledFuture.isDone()) return "选举超时已完成";
        return "选举超时：" + scheduledFuture.getDelay(TimeUnit.MILLISECONDS) + "ms 后进行";
    }
}
