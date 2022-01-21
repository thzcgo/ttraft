package com.thzc.ttraft.core.node.role;

import com.google.common.util.concurrent.FutureCallback;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskExecutor {

    // 提交任务
    Future<?> submit(Runnable task);

    <V> Future<V> submit(Callable<V> task);

    void shutdown() throws InterruptedException;



    /**
     * Submit task with callback.
     *
     * @param task     task
     * @param callback callback
     */
    void submit(@Nonnull Runnable task, @Nonnull FutureCallback<Object> callback);

    /**
     * Submit task with callbacks.
     *
     * @param task task
     * @param callbacks callbacks, should not be empty
     */
    void submit(@Nonnull Runnable task, @Nonnull Collection<FutureCallback<Object>> callbacks);


}
