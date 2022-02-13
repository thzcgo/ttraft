package com.thzc.ttraft.core.log;

import com.google.common.eventbus.EventBus;
import com.thzc.ttraft.core.log.sequence.EntrySequence;
import com.thzc.ttraft.core.log.sequence.MemoryEntrySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@NotThreadSafe
public class MemoryLog extends AbstractLog {

    private static final Logger logger = LoggerFactory.getLogger(MemoryLog.class);

    public MemoryLog() {
        this(new MemoryEntrySequence());
    }

    public MemoryLog(EntrySequence entrySequence) {
        this.entrySequence = entrySequence;
    }




}

