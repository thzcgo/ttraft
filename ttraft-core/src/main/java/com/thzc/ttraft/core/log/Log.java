package com.thzc.ttraft.core.log;

import com.thzc.ttraft.core.log.entry.Entry;
import com.thzc.ttraft.core.log.entry.EntryMeta;
import com.thzc.ttraft.core.log.entry.GeneralEntry;
import com.thzc.ttraft.core.rpc.message.AppendEntriesRpc;
import com.thzc.ttraft.core.log.entry.NoOpEntry;
import com.thzc.ttraft.core.node.NodeId;

import java.util.List;

public interface Log {

    int ALL_ENTRIES = -1;

    /*
    * 获取最后一条日志条目的term、index信息
    * */
    EntryMeta getLastEntryMeta();

    AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId, int nextIndex, int maxEntries);

    NoOpEntry appendEntry(int term);

    GeneralEntry appendEntry(int term, byte[] command);

    boolean appednEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> entries);

    void advanceCommitIndex(int newCommitIndex, int currentTerm);

    int getNextIndex();

    int getCommitIndex();

    boolean isNewerThen(int lastLogIndex, int lastLogTerm);

    void close();
}
