package com.thzc.ttraft.core.support;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.thzc.ttraft.core.support.TaskExecutor;

import javax.annotation.Nonnull;
import java.util.Collections;

public abstract class AbstractTaskExecutor implements TaskExecutor {

    @Override
    public void submit(@Nonnull Runnable task, @Nonnull FutureCallback<Object> callback) {
        Preconditions.checkNotNull(task);
        Preconditions.checkNotNull(callback);
        submit(task, Collections.singletonList(callback));
    }

}
