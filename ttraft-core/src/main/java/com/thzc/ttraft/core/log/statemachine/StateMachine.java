package com.thzc.ttraft.core.log.statemachine;

public interface StateMachine {

    int getLastApplied();

    void applyLog(int index, byte[] commandBytes);

    void shutdown();
}
