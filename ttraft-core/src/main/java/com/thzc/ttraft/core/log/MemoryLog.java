package com.thzc.ttraft.core.log;

import com.thzc.ttraft.core.log.sequence.EntrySequence;

public class MemoryLog extends AbstractLog {

    public MemoryLog() {
    }

    MemoryLog(EntrySequence entrySequence) {
        this.entrySequence = entrySequence;
    }
}

