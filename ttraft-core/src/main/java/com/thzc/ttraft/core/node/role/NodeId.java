package com.thzc.ttraft.core.node.role;

import com.google.common.base.Preconditions;
import jdk.nashorn.internal.ir.annotations.Immutable;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

@Immutable
public class NodeId implements Serializable {

    private final String value;

    public NodeId(String value) {
        Preconditions.checkNotNull(value);
        this.value = value;
    }

    public static NodeId of(@Nonnull String value) {
        return new NodeId(value);
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof NodeId)) return false;
        return Objects.equals(value, ((NodeId)o).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "NodeId{" +
                "value='" + value + '\'' +
                '}';
    }
}
