package com.thzc.ttraft.core.node;

import com.thzc.ttraft.core.log.statemachine.StateMachine;
import com.thzc.ttraft.core.node.role.RoleNameAndLeaderId;

public interface Node {

    void start();

    void stop() throws InterruptedException;

    void registerStateMachine(StateMachine stateMachine);

    void appendLog(byte[] commandBytes);

    RoleNameAndLeaderId getRoleNameAndLeaderId();
}
