package com.thzc.ttraft.core.node.role;

public class MemoryLog extends AbstractLog{

    public MemoryLog() {
    }

    MemoryLog(EntrySequence entrySequence) {
        this.entrySequence = entrySequence;
    }
}

