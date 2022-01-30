package com.thzc.ttraft.core.log.entry;

public class EntryFactory {

    public static Entry create(int kind, int index, int term, byte[] commandBytes) {
        switch (kind) {
            case Entry.KIND_NO_OP:
                return new NoOpEntry(term, index);
            case Entry.KIND_GENERAL:
                return new GeneralEntry(term, index, commandBytes);
            default:
                throw new IllegalArgumentException("类型异常");
        }
    }
}
