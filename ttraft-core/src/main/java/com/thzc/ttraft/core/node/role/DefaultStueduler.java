package com.thzc.ttraft.core.node.role;

import java.util.EventListener;
import java.util.Random;
import java.util.concurrent.*;

public class DefaultStueduler implements Scheduler{

    private final int minElectionTimeout; // 最小选举超时时间
    private final int maxElectionTimeout; // 最大选举超时时间
    private final int logReplicationDelay; // 初次日志复制延迟时间
    private final int logReplicationInterval; // 日志复制间隔
    private final Random electionTimeoutRandom; // 随机数生成器

    private final ScheduledExecutorService scheduledExecutorService;

    public DefaultStueduler(int minElectionTimeout, int maxElectionTimeout, int logReplicationDelay, int logReplicationInterval) {
        if (minElectionTimeout <= 0 || maxElectionTimeout <= 0 ||
        minElectionTimeout > maxElectionTimeout) {
            throw new IllegalArgumentException("选举时间为0 或 min > max");
        }
        if (logReplicationDelay < 0 ||
        logReplicationInterval <= 0) {
            throw new IllegalArgumentException("日志复制延迟时间,间隔不合法");
        }
        this.minElectionTimeout = minElectionTimeout;
        this.maxElectionTimeout = maxElectionTimeout;
        this.logReplicationDelay = logReplicationDelay;
        this.logReplicationInterval = logReplicationInterval;
        electionTimeoutRandom = new Random();

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
                r -> new Thread(r, "scheduler")
        );
    }

    @Override
    public ElectionTimeout schedulerElectionTimeout(Runnable task) {
        int timeout = electionTimeoutRandom.nextInt(maxElectionTimeout - minElectionTimeout) + minElectionTimeout;
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.schedule(task, timeout, TimeUnit.MILLISECONDS);
        return new ElectionTimeout(scheduledFuture);
    }

    @Override
    public LogReplicationTask schedulerLogReplicationTask(Runnable task) {
        ScheduledFuture<?> scheduledFuture =
                scheduledExecutorService.scheduleWithFixedDelay(task, logReplicationDelay, logReplicationInterval,TimeUnit.MILLISECONDS);
        return new LogReplicationTask(scheduledFuture);
    }

    @Override
    public void stop() throws InterruptedException {

    }
}
