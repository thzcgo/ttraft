package com.thzc.ttraft.core.log.statemachine;

public interface StateMachine {

    int getLastApplied();

    void applyLog(StateMachineContext context, int index, byte[] commandBytes, int firstLogIndex);

    void shutdown();
}
