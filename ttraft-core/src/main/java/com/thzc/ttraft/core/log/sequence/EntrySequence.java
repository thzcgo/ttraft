package com.thzc.ttraft.core.log.sequence;

import com.thzc.ttraft.core.log.entry.Entry;
import com.thzc.ttraft.core.log.entry.EntryMeta;

import java.util.List;

public interface EntrySequence {

    /************  日志索引相关   ****************************/
    int getFirstLogIndex();

    int getLastLogIndex();

    int getNextLogIndex();

    /************  子序列相关   ****************************/
    List<Entry> subList(int fromIndex);

    List<Entry> subList(int fromIndex, int toIndex);

    /************  日志条目相关   ****************************/
    boolean isEntryPresent(int index);

    EntryMeta getEntryMeta(int index);

    Entry getEntry(int index);

    Entry getLastEntry();

    void append(Entry entry);

    void append(List<Entry> entries);

    void removeAfter(int index);

    /************  commit相关   ****************************/
    void commit(int index);

    int getCommitIndex();

    /************  其它   ****************************/
    boolean isEmpty();

    void close();
}
