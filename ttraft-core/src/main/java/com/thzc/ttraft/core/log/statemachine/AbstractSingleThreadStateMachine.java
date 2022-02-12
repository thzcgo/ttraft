package com.thzc.ttraft.core.log.statemachine;

import com.thzc.ttraft.core.support.SingleThreadTaskExecutor;
import com.thzc.ttraft.core.support.TaskExecutor;

public abstract class AbstractSingleThreadStateMachine implements StateMachine{

    private volatile int lastApplied = 0;
    private final TaskExecutor taskExecutor;

    public AbstractSingleThreadStateMachine() {
        taskExecutor = new SingleThreadTaskExecutor("state-machine");
    }

    @Override
    public int getLastApplied() {
        return lastApplied;
    }

    @Override
    public void applyLog(int index, byte[] commandBytes) {
        taskExecutor.submit(() -> doApplyLog(index, commandBytes));
    }
    private void doApplyLog(int index, byte[] commandBytes) {
        if (index <= lastApplied) return;
        applyCommand(commandBytes);
        lastApplied = index;
    }

    protected abstract void applyCommand(byte[] commandBytes);

    @Override
    public void shutdown() {
        try {
            taskExecutor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
