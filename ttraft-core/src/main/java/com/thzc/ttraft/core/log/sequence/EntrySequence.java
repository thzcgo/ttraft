package com.thzc.ttraft.core.log.sequence;

import com.thzc.ttraft.core.log.entry.Entry;
import com.thzc.ttraft.core.log.entry.EntryMeta;

import java.util.List;

public interface EntrySequence {

    boolean isEmpty();

    int getFirstLogIndex();

    int getLastLogIndex();

    int getNextLogIndex();

    List<Entry> subList(int fromIndex);

    List<Entry> subList(int fromIndex, int toIndex);

    boolean isEntryPresent(int index);

    EntryMeta getEntryMeta(int index);

    Entry getEntry(int index);

    Entry getLastEntry();

    void append(Entry entry);

    void append(List<Entry> entries);

    void commit(int index);

    int getCommitIndex();

    void removeAfter(int index);

    void close();
}
