package com.thzc.ttraft.core.log;

import com.google.common.eventbus.EventBus;
import com.thzc.ttraft.core.log.entry.Entry;
import com.thzc.ttraft.core.log.entry.EntryMeta;
import com.thzc.ttraft.core.log.entry.GeneralEntry;
import com.thzc.ttraft.core.log.entry.NoOpEntry;
import com.thzc.ttraft.core.log.sequence.EntrySequence;
import com.thzc.ttraft.core.log.sequence.EntrySequenceView;
import com.thzc.ttraft.core.log.statemachine.EmptyStateMachine;
import com.thzc.ttraft.core.log.statemachine.StateMachine;
import com.thzc.ttraft.core.log.statemachine.StateMachineContext;
import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.rpc.message.AppendEntriesRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

abstract class AbstractLog implements Log {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLog.class);

    protected EntrySequence entrySequence;

    protected StateMachine stateMachine = new EmptyStateMachine();
    protected int commitIndex = 0;

    @Override
    @Nonnull
    public EntryMeta getLastEntryMeta() {
        if (entrySequence.isEmpty()) {
            return new EntryMeta(Entry.KIND_NO_OP, 0, 0);
        }
        return entrySequence.getLastEntry().getMeta();
    }

    @Override
    public AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId, int nextIndex, int maxEntries) {
        int nextLogIndex = entrySequence.getNextLogIndex();
        if (nextIndex > nextLogIndex) {
            throw new IllegalArgumentException("illegal next index " + nextIndex);
        }

        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setMessageId(UUID.randomUUID().toString());
        rpc.setTerm(term);
        rpc.setLeaderId(selfId);
        rpc.setLeaderCommit(commitIndex);

        // if entry sequence is empty,
        // snapshot.lastIncludedIndex + 1 == nextLogIndex,
        // so it has been rejected at the first line.
        //
        // if entry sequence is not empty,
        // snapshot.lastIncludedIndex + 1 < nextIndex <= nextLogIndex
        // and snapshot.lastIncludedIndex + 1 = firstLogIndex
        //     nextLogIndex = lastLogIndex + 1
        // then firstLogIndex < nextIndex <= lastLogIndex + 1
        //      firstLogIndex + 1 <= nextIndex <= lastLogIndex + 1
        //      firstLogIndex <= nextIndex - 1 <= lastLogIndex
        // it is ok to get entry without null check
        Entry entry = entrySequence.getEntry(nextIndex - 1);
        if (entry != null) {
            rpc.setPrevLogIndex(entry.getIndex());
            rpc.setPrevLogTerm(entry.getTerm());
        }
        if (!entrySequence.isEmpty()) {
            int maxIndex = (maxEntries == ALL_ENTRIES ? nextLogIndex : Math.min(nextLogIndex, nextIndex + maxEntries));
            rpc.setEntries(entrySequence.subList(nextIndex, maxIndex));
        }
        return rpc;
    }

    @Override
    public int getNextIndex() {
        return entrySequence.getNextLogIndex();
    }

    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

    @Override
    public boolean isNewerThan(int lastLogIndex, int lastLogTerm) {
        EntryMeta lastEntryMeta = getLastEntryMeta();
        logger.debug("last entry ({}, {}), candidate ({}, {})", lastEntryMeta.getIndex(), lastEntryMeta.getTerm(), lastLogIndex, lastLogTerm);
        return lastEntryMeta.getTerm() > lastLogTerm || lastEntryMeta.getIndex() > lastLogIndex;
    }

    @Override
    public NoOpEntry appendEntry(int term) {
        NoOpEntry entry = new NoOpEntry(entrySequence.getNextLogIndex(), term);
        entrySequence.append(entry);
        return entry;
    }

    @Override
    public GeneralEntry appendEntry(int term, byte[] command) {
        GeneralEntry entry = new GeneralEntry(entrySequence.getNextLogIndex(), term, command);
        entrySequence.append(entry);
        return entry;
    }



    @Override
    public boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> leaderEntries) {
        // check previous log
        if (!checkIfPreviousLogMatches(prevLogIndex, prevLogTerm)) {
            return false;
        }
        // heartbeat
        if (leaderEntries.isEmpty()) {
            return true;
        }
        assert prevLogIndex + 1 == leaderEntries.get(0).getIndex();
        EntrySequenceView newEntries = removeUnmatchedLog(new EntrySequenceView(leaderEntries));
        appendEntriesFromLeader(newEntries);
        return true;
    }

    private void appendEntriesFromLeader(EntrySequenceView leaderEntries) {
        if (leaderEntries.isEmpty()) {
            return;
        }
        logger.debug("append entries from leader from {} to {}", leaderEntries.getFirstLogIndex(), leaderEntries.getLastLogIndex());
        for (Entry leaderEntry : leaderEntries) {
            appendEntryFromLeader(leaderEntry);
        }
    }

    private void appendEntryFromLeader(Entry leaderEntry) {
        entrySequence.append(leaderEntry);

    }

    private EntrySequenceView removeUnmatchedLog(EntrySequenceView leaderEntries) {
        assert !leaderEntries.isEmpty();
        int firstUnmatched = findFirstUnmatchedLog(leaderEntries);
        removeEntriesAfter(firstUnmatched - 1);
        return leaderEntries.subView(firstUnmatched);
    }

    private int findFirstUnmatchedLog(EntrySequenceView leaderEntries) {
        assert !leaderEntries.isEmpty();
        int logIndex;
        EntryMeta followerEntryMeta;
        for (Entry leaderEntry : leaderEntries) {
            logIndex = leaderEntry.getIndex();
            followerEntryMeta = entrySequence.getEntryMeta(logIndex);
            if (followerEntryMeta == null || followerEntryMeta.getTerm() != leaderEntry.getTerm()) {
                return logIndex;
            }
        }
        return leaderEntries.getLastLogIndex() + 1;
    }

    private boolean checkIfPreviousLogMatches(int prevLogIndex, int prevLogTerm) {
        EntryMeta meta = entrySequence.getEntryMeta(prevLogIndex);
        if (meta == null) return false;
        int term = meta.getTerm();
        if (term != prevLogTerm) return false;
        return true;
    }

    private void removeEntriesAfter(int index) {
        if (entrySequence.isEmpty() || index >= entrySequence.getLastLogIndex()) return;
        entrySequence.removeAfter(index);
    }

    @Override
    public void advanceCommitIndex(int newCommitIndex, int currentTerm) {
        if (!validateNewCommitIndex(newCommitIndex, currentTerm)) {
            return;
        }
        logger.debug("advance commit index from {} to {}", commitIndex, newCommitIndex);
        entrySequence.commit(newCommitIndex);
        commitIndex = newCommitIndex;

        advanceApplyIndex();
    }



    private void advanceApplyIndex() {
        // start up and snapshot exists
        int lastApplied = stateMachine.getLastApplied();
        for (Entry entry : entrySequence.subList(lastApplied + 1, commitIndex + 1)) {
            applyEntry(entry);
        }
    }



    private void applyEntry(Entry entry) {
        // skip no-op entry and membership-change entry
        if (isApplicable(entry)) {
            stateMachine.applyLog(entry.getIndex(), entry.getCommandBytes());
        }
    }

    private boolean isApplicable(Entry entry) {
        return entry.getKind() == Entry.KIND_GENERAL;
    }



    private boolean validateNewCommitIndex(int newCommitIndex, int currentTerm) {
        if (newCommitIndex <= commitIndex) {
            return false;
        }
        Entry entry = entrySequence.getEntry(newCommitIndex);
        if (entry == null) {
            logger.debug("log of new commit index {} not found", newCommitIndex);
            return false;
        }
        if (entry.getTerm() != currentTerm) {
            logger.debug("log term of new commit index != current term ({} != {})", entry.getTerm(), currentTerm);
            return false;
        }
        return true;
    }



    @Override
    public void setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    public void close() {
        entrySequence.close();
        stateMachine.shutdown();
    }



}
