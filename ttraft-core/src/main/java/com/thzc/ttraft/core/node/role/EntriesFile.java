package com.thzc.ttraft.core.node.role;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EntriesFile {

    private final SeekableFile seekableFile;

    public EntriesFile(SeekableFile seekableFile) {
        this.seekableFile = seekableFile;
    }

    public EntriesFile(File file) throws FileNotFoundException {
        this(new RandomAccessFileAdapter(file));
    }

    //追加日志条目
    public long appendEntry(Entry entry) throws IOException {
        long offest = seekableFile.size();
        seekableFile.seek(offest);
        seekableFile.writeInt(entry.getKind());
        seekableFile.writeInt(entry.getIndex());
        seekableFile.writeInt(entry.getTerm());
        byte[] commandBytes = entry.getCommandBytes();
        seekableFile.writeInt(commandBytes.length);
        seekableFile.write(commandBytes);
        return offest;
    }

    // 从指定偏移加载日志条目
    public Entry loadEntry(long offset, EntryFactory factory) throws IOException {
        if (offset > seekableFile.size()) throw new IllegalArgumentException("偏移值异常");
        seekableFile.seek(offset);
        int kind = seekableFile.readInt();
        int index = seekableFile.readInt();
        int term = seekableFile.readInt();
        int length = seekableFile.readInt();
        byte[] bytes = new byte[length];
        seekableFile.read(bytes);
        return EntryFactory.create(kind, index, term, bytes);
    }

    // 获得大小
    public long size() throws IOException {
        return seekableFile.size();
    }

    // 清空内容
    public void clear() throws IOException {
        truncate(0L);
    }

    // 裁剪到指定大小，偏移由调用者提供
    public void truncate(long offset) throws IOException {
        seekableFile.truncate(offset);
    }

    // 关闭文件
    public void close() throws IOException {
        seekableFile.close();
    }
}
