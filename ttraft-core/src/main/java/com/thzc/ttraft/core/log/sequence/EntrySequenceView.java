package com.thzc.ttraft.core.log.sequence;

import com.thzc.ttraft.core.log.entry.Entry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class EntrySequenceView implements Iterable<Entry> {

    private final List<Entry> entries;
    private int firstLogIndex = -1;
    private int lastLogIndex = -1;

    public EntrySequenceView(List<Entry> Entries) {
        this.entries = Entries;
        if (!entries.isEmpty()) {
            firstLogIndex = entries.get(0).getIndex();
            lastLogIndex = entries.get(entries.size() - 1).getIndex();
        }
    }

    Entry get(int index) {
        if (entries.isEmpty() || index < firstLogIndex || index > lastLogIndex) return null;
        return entries.get(index);
    }

    int getFirstLogIndex() {
        return firstLogIndex;
    }

    int getLastLogIndex() {
        return lastLogIndex;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public EntrySequenceView subView(int fromIndex) {
        if (entries.isEmpty() || fromIndex > lastLogIndex) {
            return new EntrySequenceView(Collections.emptyList());
        }
        return new EntrySequenceView(entries.subList(fromIndex - firstLogIndex, entries.size()));
    }

    @Nonnull
    public Iterator<Entry> iterator() {
        return entries.iterator();
    }
}
