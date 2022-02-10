package com.thzc.ttraft.core.log;

import com.thzc.ttraft.core.log.dir.LogGeneration;
import com.thzc.ttraft.core.log.dir.RootDir;
import com.thzc.ttraft.core.log.sequence.FileEntrySequence;

import java.io.File;

public class FileLog extends AbstractLog {

    private final RootDir rootDir;

    public FileLog(File baseDir) {
        rootDir = new RootDir(baseDir);
        LogGeneration latestGeneration = rootDir.getLatestGeneration();
        if (latestGeneration != null) {
            entrySequence = new FileEntrySequence(latestGeneration, latestGeneration.getLastIncludedIndex());
        } else {
            LogGeneration firstGeneration = rootDir.createFirstGeneration();
            entrySequence = new FileEntrySequence(firstGeneration, 1);
        }
    }
}
