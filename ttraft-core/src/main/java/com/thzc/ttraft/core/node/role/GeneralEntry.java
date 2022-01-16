package com.thzc.ttraft.core.node.role;

public class GeneralEntry extends AbstractEntry{

    private final byte[] commandBytes;

    public GeneralEntry(int term, int index, byte[] commandBytes) {
        super(KIND_GENERAL, term, index);
        this.commandBytes = commandBytes;
    }

    @Override
    public byte[] getCommandBytes() {
        return commandBytes;
    }

    @Override
    public String toString() {
        return "GeneralEntry{" +
                "term=" + term +
                ", index=" + index +
                '}';
    }
}
