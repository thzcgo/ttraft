package com.thzc.ttraft.core.log.entry;

import com.thzc.ttraft.core.log.entry.AbstractEntry;

public class NoOpEntry extends AbstractEntry {

    public NoOpEntry(int term, int index) {
        super(KIND_NO_OP, term, index);
    }

    @Override
    public byte[] getCommandBytes() {
        return new byte[0];
    }

    @Override
    public String toString() {
        return "NoOpEntry{" +
                "term=" + term +
                ", index=" + index +
                '}';
    }
}
