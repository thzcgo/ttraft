package com.thzc.ttraft.core.node.role;

import com.google.common.eventbus.EventBus;

import java.io.File;

public class FileLog extends AbstractLog{

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
