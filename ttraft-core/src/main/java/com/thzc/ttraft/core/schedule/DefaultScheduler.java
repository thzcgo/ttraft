package com.thzc.ttraft.core.schedule;

import com.thzc.ttraft.core.node.config.NodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.*;

public class DefaultScheduler implements Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultScheduler.class);

    private final int minElectionTimeout; // 最小选举超时时间
    private final int maxElectionTimeout; // 最大选举超时时间
    private final int logReplicationDelay; // 初次日志复制延迟时间
    private final int logReplicationInterval; // 日志复制间隔
    private final Random electionTimeoutRandom; // 随机数生成器

    private final ScheduledExecutorService scheduledExecutorService;

    /*
    *  从配置文件构造定时器
    * */
    public DefaultScheduler(NodeConfig config) {
        this(config.getMinElectionTimeout(), config.getMaxElectionTimeout(), config.getLogReplicationDelay(),
                config.getLogReplicationInterval());
    }

    /*
     *  直接传参构造定时器
     * */
    public DefaultScheduler(int minElectionTimeout, int maxElectionTimeout, int logReplicationDelay, int logReplicationInterval) {
        if (minElectionTimeout <= 0 || maxElectionTimeout <= 0 ||
        minElectionTimeout > maxElectionTimeout) {
            throw new IllegalArgumentException("最小或最大选举时间不合法");
        }
        if (logReplicationDelay < 0 ||
        logReplicationInterval <= 0) {
            throw new IllegalArgumentException("日志复制延迟时间或间隔不合法");
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
    public ElectionTimeout scheduleElectionTimeout(Runnable task) {
        logger.debug("schedule election timeout");
        // 在范围内随机一个超时时间，有效避免多个节点同时超时变成candidate
        int timeout = electionTimeoutRandom.nextInt(maxElectionTimeout - minElectionTimeout) + minElectionTimeout;
        ScheduledFuture<?> scheduledFuture = scheduledExecutorService.schedule(task, timeout, TimeUnit.MILLISECONDS);
        return new ElectionTimeout(scheduledFuture);
    }

    @Override
    public LogReplicationTask scheduleLogReplicationTask(Runnable task) {
        logger.debug("schedule log replication task");
        ScheduledFuture<?> scheduledFuture =
                scheduledExecutorService.scheduleWithFixedDelay(task, logReplicationDelay, logReplicationInterval,TimeUnit.MILLISECONDS);
        return new LogReplicationTask(scheduledFuture);
    }

    @Override
    public void stop() throws InterruptedException {
        logger.debug("stop scheduler");
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS);
    }
}
