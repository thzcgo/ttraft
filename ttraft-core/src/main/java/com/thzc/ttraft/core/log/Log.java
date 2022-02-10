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

    /*******  追加日志项  **************************************************/
    NoOpEntry appendEntry(int term);

    GeneralEntry appendEntry(int term, byte[] command);

    boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> entries);

    /*******  CommitIndex 相关  ******************************************/
    int getCommitIndex();

    void advanceCommitIndex(int newCommitIndex, int currentTerm);

    /*******  其他  ******************************************/
    EntryMeta getLastEntryMeta();

    AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId, int nextIndex, int maxEntries);

    int getNextIndex();

    boolean isNewerThen(int lastLogIndex, int lastLogTerm);

    void close();
}
