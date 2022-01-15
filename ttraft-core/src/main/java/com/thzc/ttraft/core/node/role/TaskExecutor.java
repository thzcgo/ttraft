package com.thzc.ttraft.core.node.role;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskExecutor {

    // 提交任务
    Future<?> submit(Runnable task);

    <V> Future<V> submit(Callable<V> task);

    void shutdown() throws InterruptedException;
}
