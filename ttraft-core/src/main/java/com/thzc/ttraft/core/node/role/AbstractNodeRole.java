package com.thzc.ttraft.core.node.role;

import com.thzc.ttraft.core.node.NodeId;

public abstract class AbstractNodeRole {

    private final RoleName name;
    protected final int term;

    public AbstractNodeRole(RoleName name, int term) {
        this.name = name;
        this.term = term;
    }

    // 取消超时定时任务
    public abstract void cancelTimeoutOrTask();

    public RoleNameAndLeaderId getNameAndLeaderId(NodeId selfId) {
        return new RoleNameAndLeaderId(name, getLeaderId(selfId));
    }

    public abstract NodeId getLeaderId(NodeId selfId);

    public RoleName getName() {
        return name;
    }

    public int getTerm() {
        return term;
    }

    public abstract RoleState getState();

    public boolean stateEquals(AbstractNodeRole that) {
        if (this.name != that.name || this.term != that.term) {
            return false;
        }
        return doStateEquals(that);
    }

    protected abstract boolean doStateEquals(AbstractNodeRole role);
}
