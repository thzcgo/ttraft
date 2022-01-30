package com.thzc.ttraft.core.log.sequence;

import com.thzc.ttraft.core.log.LogDir;
import com.thzc.ttraft.core.log.LogException;
import com.thzc.ttraft.core.log.entry.Entry;
import com.thzc.ttraft.core.log.entry.EntryMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FileEntrySequence extends AbstractEntrySequence {
    
    private final EntriesFile entriesFile;
    private final EntryIndexFile entryIndexFile;
    private final LinkedList<Entry> pendingEntries = new LinkedList<>();
    
    private int commitIndex = 0;
    
    public FileEntrySequence(LogDir logDir, int logIndexOffset) {
        super(logIndexOffset);
        try {
            this.entriesFile = new EntriesFile(logDir.getEntriesFile());
            this.entryIndexFile = new EntryIndexFile(logDir.getEntryOffsetIndexFile());
            initialize();
        } catch (IOException e) {
            throw new LogException("日志文件或日志索引文件异常");
        }
    }

    public FileEntrySequence(EntriesFile entriesFile, EntryIndexFile entryIndexFile,int logIndexOffset) {
        super(logIndexOffset);
        this.entriesFile = entriesFile;
        this.entryIndexFile = entryIndexFile;
        initialize();
    }

    private void initialize() {
        if (entryIndexFile.isEmpty()) return;
        logIndexOffset = entryIndexFile.getMinEntryIndex();
        nextLogIndex = entryIndexFile.getMaxEntryIndex() + 1;
    }

    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

    @Override
    protected List<Entry> doSubList(int fromIndex, int toIndex) {
        List<Entry> result = new ArrayList<>();
        // 从文件中获取
        if (!entryIndexFile.isEmpty() && fromIndex <= entryIndexFile.getMaxEntryIndex()) {
            int maxIndex = Math.min(entryIndexFile.getMaxEntryIndex() + 1, toIndex);
            for (int i = fromIndex; i < maxIndex; i++) {
                result.add(getEntryInFile(i));
            }
        }
        // 从日志缓冲中获取日志
        if (!pendingEntries.isEmpty() && toIndex > pendingEntries.getFirst().getIndex()) {
            Iterator<Entry> iterator = pendingEntries.iterator();
            Entry entry;
            int index;
            while (iterator.hasNext()) {
                entry = iterator.next();
                index = entry.getIndex();
                if (index >= toIndex) break;;
                if (index >= fromIndex) result.add(entry);
            }
        }
        return result;
    }

    private Entry getEntryInFile(int index) {
        long offset = entryIndexFile.getOffset(index);
        try {
            return entriesFile.loadEntry(offset);
        } catch (IOException e) {
            throw new LogException("fail to load entry");
        }
    }

    @Override
    protected Entry doGetEntry(int index) {
        if (!pendingEntries.isEmpty()) {
            int firstPendingEntryIndex = pendingEntries.getFirst().getIndex();
            if (index >= firstPendingEntryIndex) {
                return pendingEntries.get(index - firstPendingEntryIndex);
            }
        }
        return getEntryInFile(index);
    }

    public EntryMeta getEntryMeta(int index) {
        if (!isEntryPresent(index)) return null;
        if (!pendingEntries.isEmpty()) {
            int firstPendingEntryIndex = pendingEntries.getFirst().getIndex();
            if (index >= firstPendingEntryIndex) {
                return pendingEntries.get(index - firstPendingEntryIndex).getMeta();
            }
        }
        return getEntryInFile(index).getMeta();
    }

    public Entry getLastEntry() {
        if (!isEmpty()) return null;
        if (!pendingEntries.isEmpty()) return pendingEntries.getLast();
        return getEntryInFile(entryIndexFile.getMaxEntryIndex());
    }

    @Override
    protected void doAppend(Entry entry) {
        pendingEntries.add(entry);
    }

    @Override
    protected void doRemoveAfter(int index) {
        // 只需要移除缓冲中的日志
        if (!pendingEntries.isEmpty() && index >= pendingEntries.getFirst().getIndex() - 1) {
            for(int i = index + 1; i <= doGetLastLogIndex(); i++) {
                pendingEntries.removeLast();
            }
            nextLogIndex = index + 1;
            return;
        }
        try {
            if (index >= doGetFirstLogIndex()) {
                pendingEntries.clear();
                entriesFile.truncate(entryIndexFile.getOffset(index + 1));
                entryIndexFile.removeAfter(index);
                nextLogIndex = index + 1;
                commitIndex = index;
            } else {
                pendingEntries.clear();
                entryIndexFile.clear();
                entriesFile.clear();
                nextLogIndex = logIndexOffset;
                commitIndex = logIndexOffset - 1;
            }
        } catch (IOException e) {
            throw new LogException();
        }
    }

    public void commit(int index) {
        if (index < commitIndex) throw new IllegalArgumentException("index 异常");
        if (index == commitIndex) return;
        // 如果 commitIndex 在文件内，就只更新 commitIndex
        if (!entryIndexFile.isEmpty() && index <= entryIndexFile.getMaxEntryIndex()) {
            commitIndex = index;
            return;
        }

        if (pendingEntries.isEmpty() ||
                pendingEntries.getFirst().getIndex() > index ||
                pendingEntries.getLast().getIndex() < index) {
            throw new IllegalArgumentException("没有可commit的entry");
        }
        long offset;
        Entry entry = null;
        try {
            for (int i = pendingEntries.getFirst().getIndex(); i <= index; i++) {
                entry = pendingEntries.removeFirst();
                offset = entriesFile.appendEntry(entry);
                entryIndexFile.appendEntryIndex(i ,offset, entry.getKind(), entry.getTerm());
            }
        } catch (IOException e) {
            throw new LogException("commit 异常");
        }
    }
}
