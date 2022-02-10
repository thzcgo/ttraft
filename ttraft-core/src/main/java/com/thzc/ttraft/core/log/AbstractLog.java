package com.thzc.ttraft.core.log;

import com.thzc.ttraft.core.log.entry.Entry;
import com.thzc.ttraft.core.log.entry.EntryMeta;
import com.thzc.ttraft.core.log.entry.GeneralEntry;
import com.thzc.ttraft.core.log.entry.NoOpEntry;
import com.thzc.ttraft.core.log.sequence.EntrySequence;
import com.thzc.ttraft.core.log.sequence.EntrySequenceView;
import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.rpc.message.AppendEntriesRpc;

import java.util.Collections;
import java.util.List;

public class AbstractLog implements Log {

    protected EntrySequence entrySequence;
    protected int commitIndex = 0;

    /*******  追加日志项  **************************************************/
    @Override
    public NoOpEntry appendEntry(int term) {
        NoOpEntry noOpEntry = new NoOpEntry(entrySequence.getNextLogIndex(), term);
        entrySequence.append(noOpEntry);
        return noOpEntry;
    }

    @Override
    public GeneralEntry appendEntry(int term, byte[] command) {
        GeneralEntry generalEntry = new GeneralEntry(entrySequence.getNextLogIndex(), term, command);
        entrySequence.append(generalEntry);
        return generalEntry;
    }

    @Override
    public boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> leaderEntries) {
        // 检查前一条日志是否匹配
        if (!checkIfPreviousLogMatches(prevLogIndex, prevLogTerm)) return false;
        // Leader节点传来的是日志条目为空
        if (leaderEntries.isEmpty()) return true;
        // 移除冲突的日志条目，返回要追加的日志条目
        EntrySequenceView newEntries = removeUnmatchedLog(new EntrySequenceView(leaderEntries));
        appednEntriesFromLeader(newEntries);
        return false;
    }

    private void appednEntriesFromLeader(EntrySequenceView newEntries) {
        if (newEntries.isEmpty()) return;
        for (Entry leaderEntry : newEntries) {
            entrySequence.append(leaderEntry);
        }
    }

    private boolean checkIfPreviousLogMatches(int prevLogIndex, int prevLogTerm) {
        EntryMeta meta = entrySequence.getEntryMeta(prevLogIndex);
        if (meta != null) return false;
        int term = meta.getTerm();
        if (term != prevLogTerm) return false;
        return true;
    }

    // 移除不一致的操作
    private EntrySequenceView removeUnmatchedLog(EntrySequenceView leaderEntries) {
        int firstUnmatched = findFirstUnmatchedLog(leaderEntries);
        if (firstUnmatched < 0) return new EntrySequenceView(Collections.emptyList());
        removeEntriesAfter(firstUnmatched - 1);
        return leaderEntries.subView(firstUnmatched);
    }

    private int findFirstUnmatchedLog(EntrySequenceView leaderEntries) {
        int logIndex;
        EntryMeta followerEntryMeta;
        for (Entry leaderEntry : leaderEntries) {
            logIndex = leaderEntry.getIndex();
            followerEntryMeta = entrySequence.getEntryMeta(logIndex);
            if (followerEntryMeta == null || followerEntryMeta.getTerm() != leaderEntry.getTerm()) return logIndex;
        }
        return -1;
    }

    private void removeEntriesAfter(int index) {
        if (entrySequence.isEmpty() || index >= entrySequence.getLastLogIndex()) return;
        entrySequence.removeAfter(index);
    }

    /*******  CommitIndex 相关  ******************************************/
    @Override
    public int getCommitIndex() {
        return 0;
    }

    @Override
    public void advanceCommitIndex(int newCommitIndex, int currentTerm) {
        if (!validateNewCommitIndex(newCommitIndex, currentTerm)) return;
        entrySequence.commit(newCommitIndex);
    }

    private boolean validateNewCommitIndex(int newCommitIndex, int currentTerm) {
        if (newCommitIndex <= entrySequence.getCommitIndex()) return false;
        EntryMeta meta = entrySequence.getEntryMeta(newCommitIndex);
        if (meta == null) return false;
        if (meta.getTerm() != currentTerm) return false;
        return true;
    }

    /*******  其他  ******************************************/
    @Override
    public EntryMeta getLastEntryMeta() {
        if (entrySequence.isEmpty()) return new EntryMeta(Entry.KIND_NO_OP, 0, 0);
        return entrySequence.getLastEntry().getMeta();
    }

    @Override
    public AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId, int nextIndex, int maxEntries) {
        int nextLogIndex = entrySequence.getNextLogIndex();
        if (nextLogIndex < nextIndex) {
            throw new IllegalArgumentException("nextIndex参数异常");
        }
        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setTerm(term);
        rpc.setLeaderId(selfId);
        rpc.setLeaderCommit(commitIndex);
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
        return 0;
    }

    @Override
    public boolean isNewerThen(int lastLogIndex, int lastLogTerm) {
        EntryMeta entryMeta = getLastEntryMeta();
        return getLastEntryMeta().getTerm() > lastLogTerm || getLastEntryMeta().getIndex() > lastLogIndex;
    }

    @Override
    public void close() {

    }
}
