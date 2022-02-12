package com.thzc.ttraft.core.log;

import com.thzc.ttraft.core.log.sequence.EntrySequence;
import com.thzc.ttraft.core.log.sequence.MemoryEntrySequence;

public class MemoryLog extends AbstractLog {

    public MemoryLog() {
        this.entrySequence = new MemoryEntrySequence();
    }

    MemoryLog(EntrySequence entrySequence) {
        this.entrySequence = entrySequence;
    }
}

