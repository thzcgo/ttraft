package com.thzc.ttraft.core.support;

import com.google.common.util.concurrent.FutureCallback;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskExecutor {

    Future<?> submit(Runnable task);

    <V> Future<V> submit(Callable<V> task);

    void shutdown() throws InterruptedException;

    void submit(@Nonnull Runnable task, @Nonnull FutureCallback<Object> callback);

    void submit(@Nonnull Runnable task, @Nonnull Collection<FutureCallback<Object>> callbacks);
}
