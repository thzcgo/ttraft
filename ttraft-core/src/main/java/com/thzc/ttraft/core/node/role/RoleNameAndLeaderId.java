package com.thzc.ttraft.core.node.role;

import com.google.common.base.Preconditions;
import com.thzc.ttraft.core.node.NodeId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public class RoleNameAndLeaderId {

    private final RoleName roleName;
    private final NodeId leaderId;

    public RoleNameAndLeaderId(@Nonnull RoleName roleName, @Nullable NodeId leaderId) {
        Preconditions.checkNotNull(roleName);
        this.roleName = roleName;
        this.leaderId = leaderId;
    }

    @Nonnull
    public RoleName getRoleName() {
        return roleName;
    }

    @Nullable
    public NodeId getLeaderId() {
        return leaderId;
    }

}
