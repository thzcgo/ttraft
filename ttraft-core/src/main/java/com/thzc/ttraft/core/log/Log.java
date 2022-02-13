package com.thzc.ttraft.core.log;

import com.thzc.ttraft.core.log.entry.Entry;
import com.thzc.ttraft.core.log.entry.EntryMeta;
import com.thzc.ttraft.core.log.entry.GeneralEntry;
import com.thzc.ttraft.core.log.statemachine.StateMachine;
import com.thzc.ttraft.core.rpc.message.AppendEntriesRpc;
import com.thzc.ttraft.core.log.entry.NoOpEntry;
import com.thzc.ttraft.core.node.NodeId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface Log {

    int ALL_ENTRIES = -1;

    EntryMeta getLastEntryMeta();

    AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId, int nextIndex, int maxEntries);

    int getNextIndex();

    int getCommitIndex();

    /**
     * Test if last log self is new than last log of leader.
     *
     * @param lastLogIndex last log index
     * @param lastLogTerm  last log term
     * @return true if last log self is newer than last log of leader, otherwise false
     */
    boolean isNewerThan(int lastLogIndex, int lastLogTerm);

    /**
     * Append a NO-OP log entry.
     *
     * @param term current term
     * @return no-op entry
     */
    NoOpEntry appendEntry(int term);

    /**
     * Append a general log entry.
     *
     * @param term    current term
     * @param command command in bytes
     * @return general entry
     */
    GeneralEntry appendEntry(int term, byte[] command);

    boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> entries);

    /**
     * Advance commit index.
     *
     * <p>
     * The log entry with new commit index must be the same term as the one in parameter,
     * otherwise commit index will not change.
     * </p>
     *
     * @param newCommitIndex new commit index
     * @param currentTerm    current term
     */
    void advanceCommitIndex(int newCommitIndex, int currentTerm);

    /**
     * Set state machine.
     * <p>
     * It will be called when
     * <ul>
     * <li>apply the log entry</li>
     * <li>generate snapshot</li>
     * <li>apply snapshot</li>
     * </ul>
     *
     * @param stateMachine state machine
     */
    void setStateMachine(StateMachine stateMachine);

    /**
     * Close log files.
     */
    void close();
}
