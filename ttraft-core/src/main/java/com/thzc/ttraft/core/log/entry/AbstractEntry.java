package com.thzc.ttraft.core.log.entry;

public class AbstractEntry implements Entry {

    private final int kind;
    protected final int term;
    protected final int index;

    public AbstractEntry(int kind, int term, int index) {
        this.kind = kind;
        this.term = term;
        this.index = index;
    }

    @Override
    public int getKind() {
        return this.kind;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public int getTerm() {
        return this.term;
    }

    @Override
    public EntryMeta getMeta() {
        return new EntryMeta(kind,index,term);
    }

    @Override
    public byte[] getCommandBytes() {
        return new byte[0];
    }
}
