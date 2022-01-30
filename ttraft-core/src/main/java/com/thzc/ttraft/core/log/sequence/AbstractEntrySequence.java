package com.thzc.ttraft.core.log.sequence;

import com.thzc.ttraft.core.log.entry.Entry;
import com.thzc.ttraft.core.log.entry.EntryMeta;

import java.util.Collections;
import java.util.List;

public abstract class AbstractEntrySequence implements EntrySequence {
    int logIndexOffset;
    int nextLogIndex;

    public AbstractEntrySequence(int logIndexOffset) {
        this.logIndexOffset = logIndexOffset;
        this.nextLogIndex = logIndexOffset;
    }

    /*******  索引相关  ******************************************************/
    @Override
    public boolean isEmpty() {
        return logIndexOffset == nextLogIndex;
    }

    @Override
    public int getFirstLogIndex() {
        if (isEmpty()) throw new EmptySequenceException();
        return doGetFirstLogIndex();
    }

    protected int doGetFirstLogIndex() {
        return logIndexOffset;
    }

    @Override
    public int getLastLogIndex() {
        if (isEmpty()) throw new EmptySequenceException();
        return doGetLastLogIndex();
    }

    public int doGetLastLogIndex() {
        return nextLogIndex - 1;
    }

    @Override
    public int getNextLogIndex() {
        return nextLogIndex;
    }

    @Override
    public boolean isEntryPresent(int index) {
        return !isEmpty() && index >= doGetFirstLogIndex() && index <= doGetLastLogIndex();
    }

    /*******  日志序列子视图相关  ******************************************************/
    protected abstract List<Entry> doSubList(int fromIndex, int toIndex);

    @Override
    public List<Entry> subList(int fromIndex) {
        if (isEmpty() || fromIndex > doGetLastLogIndex()) return Collections.emptyList();
        return subList(Math.max(fromIndex, doGetFirstLogIndex()), fromIndex);
    }

    @Override
    public List<Entry> subList(int fromIndex, int toIndex) {
        if (isEmpty()) throw new EmptySequenceException();
        if (toIndex > doGetLastLogIndex() + 1 || fromIndex < doGetFirstLogIndex() || fromIndex > toIndex) {
            throw new IllegalArgumentException("索引参数异常");
        }
        return doSubList(fromIndex, toIndex);
    }

    /*******  日志条目相关  ******************************************************/
    protected abstract Entry doGetEntry(int index);

    @Override
    public EntryMeta getEntryMeta(int index) {
        Entry entry = doGetEntry(index);
        return entry == null ? null : entry.getMeta();
    }

    @Override
    public Entry getEntry(int index) {
        if (!isEntryPresent(index)) return null;
        return doGetEntry(index);
    }

    @Override
    public Entry getLastEntry() {
        return isEmpty() ? null : doGetEntry(doGetLastLogIndex());
    }

    protected abstract void doAppend(Entry entry);

    @Override
    public void append(Entry entry) {
        if (entry.getIndex() != nextLogIndex) throw new IllegalArgumentException("日志条目索引异常");
        doAppend(entry);
        nextLogIndex++;
    }

    @Override
    public void append(List<Entry> entries) {
        for (Entry entry : entries) {
            append(entry);
        }
    }

    protected abstract void doRemoveAfter(int index);

    @Override
    public void removeAfter(int index) {
        if (isEmpty() || index >= doGetLastLogIndex()) return;
        doRemoveAfter(index);
    }

    @Override
    public void commit(int index) {

    }

    @Override
    public int getCommitIndex() {
        return 0;
    }


    @Override
    public void close() {

    }
}
