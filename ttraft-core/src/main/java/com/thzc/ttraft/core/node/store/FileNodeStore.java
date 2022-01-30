package com.thzc.ttraft.core.node.store;

import com.google.common.io.Files;
import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.support.RandomAccessFileAdapter;
import com.thzc.ttraft.core.support.SeekableFile;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.IOException;

@NotThreadSafe
public class FileNodeStore implements NodeStore {

    public static final String FILE_NAME = "node.bin";
    private static final long OFFSET_TERM = 0;
    private static final long OFFSET_VOTED_FOR = 4;
    private SeekableFile seekableFile;
    private int term = 0;
    private NodeId votedFor = null;

    public FileNodeStore(File file) {
        if (!file.exists()) {
            // 文件不存在时创建文件
            try {
                Files.touch(file);
                seekableFile = new RandomAccessFileAdapter(file);
                initalizeOrLoad();
            } catch (IOException e) {
                throw new NodeStoreException(e);
            }
        }
    }

    private void initalizeOrLoad() throws IOException {
        if (seekableFile.size() == 0) {
            seekableFile.truncate(8L);
            seekableFile.seek(0);
            seekableFile.writeInt(0);
            seekableFile.writeInt(0);
        } else {
            term = seekableFile.readInt();
            int length = seekableFile.readInt();
            if (length > 0) {
                byte[] bytes = new byte[length];
                seekableFile.read(bytes);
                votedFor = new NodeId(new String(bytes));
            }
        }
    }

    // 测试
    public FileNodeStore(SeekableFile file) {
        this.seekableFile = file;
        try {
            initalizeOrLoad();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getTerm() {
        return term;
    }

    @Override
    public void setTerm(int term) {
        try {
            seekableFile.seek(OFFSET_TERM);
            seekableFile.writeInt(term);
        } catch (IOException e) {
            throw new NodeStoreException(e);
        }
        this.term = term;
    }

    @Override
    public NodeId getVotedFor() {
        return votedFor;
    }

    @Override
    public void setVotedFor(NodeId votedFor) {
        try {
            seekableFile.seek(OFFSET_VOTED_FOR);
            if (votedFor == null) {
                seekableFile.writeInt(0);
                seekableFile.truncate(8L);
            } else {
                byte[] bytes = votedFor.getValue().getBytes();
                seekableFile.writeInt(bytes.length);
                seekableFile.write(bytes);
            }
        } catch (IOException e) {
            throw new NodeStoreException(e);
        }
       this.votedFor = votedFor;
    }

    @Override
    public void close() {
        try {
            seekableFile.close();
        } catch (IOException e) {
            throw new NodeStoreException(e);
        }
    }
}
