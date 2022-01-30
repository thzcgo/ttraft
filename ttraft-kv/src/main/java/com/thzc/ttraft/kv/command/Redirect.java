package com.thzc.ttraft.kv.command;

import com.thzc.ttraft.core.node.NodeId;

public class Redirect {

    private final String leaderId;

    public Redirect(String leaderId) {
        this.leaderId = leaderId;
    }

    public Redirect(NodeId leaderId) {
        this(leaderId != null ? leaderId.getValue() : null);
    }

    public String getLeaderId() {
        return leaderId;
    }

    @Override
    public String toString() {
        return "Redirect{" +
                "leaderId='" + leaderId + '\'' +
                '}';
    }
}
