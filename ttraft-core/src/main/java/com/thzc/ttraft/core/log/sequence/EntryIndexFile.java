package com.thzc.ttraft.core.log.sequence;

import com.thzc.ttraft.core.support.RandomAccessFileAdapter;
import com.thzc.ttraft.core.support.SeekableFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EntryIndexFile implements Iterable<EntryIndexItem>{

    // 最大条目索引的偏移
    private static final long OFFSET_MAX_ENTRY_INDEX = Integer.BYTES;
    // 单条日志条目元信息的长度
    private static final int LENGTH_ENTRY_INDEX_ITEM = 16;

    private final SeekableFile seekableFile;
    private int entryIndexCount;
    private int minEntryIndex;
    private int maxEntryIndex;
    private Map<Integer, EntryIndexItem> entryIndexItemMap = new HashMap<>();

    public EntryIndexFile(SeekableFile seekableFile) {
        this.seekableFile = seekableFile;
    }

    public EntryIndexFile(File file) throws FileNotFoundException {
        this(new RandomAccessFileAdapter(file));
    }

    // 加载所有日志条目原信息
    private void load() throws IOException {
        if (seekableFile.size() == 0L) {
            entryIndexCount = 0;
            return;
        }
        minEntryIndex = seekableFile.readInt();
        maxEntryIndex = seekableFile.readInt();
        updateEntryIndexCount();
        long offset;
        int kind;
        int term;
        for (int index = minEntryIndex; index <= maxEntryIndex; index++) {
            offset = seekableFile.readLong();
            kind = seekableFile.readInt();
            term = seekableFile.readInt();
            entryIndexItemMap.put(index, new EntryIndexItem(kind, index , term, offset));
        }
    }

    private void updateEntryIndexCount() {
        entryIndexCount = maxEntryIndex - minEntryIndex + 1;
    }

    // 追加日志条目原信息
    public void appendEntryIndex(int index, long offset, int kind, int term) throws IOException {
        if (seekableFile.size() == 0L) {
            seekableFile.writeInt(index);
            minEntryIndex = index;
        } else {
            if (index != maxEntryIndex + 1) {
                throw new IllegalArgumentException("索引异常");
            }
            seekableFile.seek(OFFSET_MAX_ENTRY_INDEX);
        }
        seekableFile.writeInt(index);
        maxEntryIndex = index;
        updateEntryIndexCount();
        seekableFile.seek(getOffsetOfEntryIndexItem(index));
        seekableFile.writeLong(offset);
        seekableFile.writeInt(kind);
        seekableFile.writeInt(term);
    }

    // 获得指定索引日志偏移
    private long getOffsetOfEntryIndexItem(int index) {
        return (index - minEntryIndex) * LENGTH_ENTRY_INDEX_ITEM + Integer.BYTES * 2;
    }

    // 移除
    public void clear() throws IOException {
        seekableFile.truncate(0L);
        entryIndexCount = 0;
        entryIndexItemMap.clear();
    }

    public boolean isEmpty() {
        return entryIndexCount == 0;
    }

    // 移除某个索引之后的数据
    public void removeAfter(int newMaxEntryIndex) throws IOException {
        if (isEmpty() || newMaxEntryIndex >= maxEntryIndex) return;
        if (newMaxEntryIndex < minEntryIndex) {
            clear();
            return;
        }
        seekableFile.seek(OFFSET_MAX_ENTRY_INDEX);
        seekableFile.writeInt(newMaxEntryIndex);
        seekableFile.truncate(getOffsetOfEntryIndexItem(newMaxEntryIndex + 1));
        for (int i = newMaxEntryIndex + 1; i <= maxEntryIndex; i++) {
            entryIndexItemMap.remove(i);
        }
        maxEntryIndex = newMaxEntryIndex;
        entryIndexCount = newMaxEntryIndex - minEntryIndex + 1;
    }

    public EntryIndexItem get(int entryIndex) {
        if (entryIndex < minEntryIndex || entryIndex > maxEntryIndex) {
            throw new IllegalArgumentException("index < min or index > max");
        }
        return entryIndexItemMap.get(entryIndex);
    }

    public int getMinEntryIndex() {
        return minEntryIndex;
    }

    public int getMaxEntryIndex() {
        return maxEntryIndex;
    }

    public long getOffset(int entryIndex) {
        return get(entryIndex).getOffset();
    }

    /*******  迭代器  *******************************************/
    public Iterator<EntryIndexItem> iterator() {
        if (isEmpty()) {
            return Collections.emptyIterator();
        }
        return new EntryIndexIterator(entryIndexCount, minEntryIndex);
    }

    private class EntryIndexIterator implements Iterator<EntryIndexItem> {
        private final int entryIndexCount;

        private int currentEntryIndex;

        public EntryIndexIterator(int entryIndexCount, int minEntryIndex) {
            this.entryIndexCount = entryIndexCount;
            this.currentEntryIndex = minEntryIndex;
        }

        @Override
        public boolean hasNext() {
            checkModification();
            return currentEntryIndex <= maxEntryIndex;
        }

        @Override
        public EntryIndexItem next() {
            checkModification();
            return entryIndexItemMap.get(currentEntryIndex++);
        }
        private void checkModification() {
            if (this.entryIndexCount != EntryIndexFile.this.entryIndexCount) {
                throw new IllegalStateException("日志条目数已改变");
            }
        }
    }

}
