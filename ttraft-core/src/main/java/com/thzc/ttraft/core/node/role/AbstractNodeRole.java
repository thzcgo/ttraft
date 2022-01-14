package com.thzc.ttraft.core.node.role;

public abstract class AbstractNodeRole {

    private final RoleName name;
    protected final int term;

    public AbstractNodeRole(RoleName name, int term) {
        this.name = name;
        this.term = term;
    }

    // 取消超时定时任务
    public abstract void cancelTimeoutOrTask();

    public RoleName getName() {
        return name;
    }

    public int getTerm() {
        return term;
    }
}
