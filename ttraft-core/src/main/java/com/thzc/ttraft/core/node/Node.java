package com.thzc.ttraft.core.node;

import com.thzc.ttraft.core.log.statemachine.StateMachine;
import com.thzc.ttraft.core.node.role.RoleNameAndLeaderId;

public interface Node {

    void registerStateMachine(StateMachine stateMachine);

    void start();

    void appendLog(byte[] commandBytes);

    void stop() throws InterruptedException;

    RoleNameAndLeaderId getRoleNameAndLeaderId();
}
