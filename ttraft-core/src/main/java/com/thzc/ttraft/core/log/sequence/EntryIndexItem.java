package com.thzc.ttraft.core.log.sequence;

import com.thzc.ttraft.core.log.entry.EntryMeta;

public class EntryIndexItem {

    private final int kind;
    private final int index;
    private final int term;
    private final long offset;

    public EntryIndexItem(int kind, int index, int term, long offset) {
        this.kind = kind;
        this.index = index;
        this.term = term;
        this.offset = offset;
    }

    int getIndex() {
        return index;
    }

    long getOffset() {
        return offset;
    }

    int getKind() {
        return kind;
    }

    int getTerm() {
        return term;
    }

    EntryMeta toEntryMeta() {
        return new EntryMeta(kind, index, term);
    }
}
