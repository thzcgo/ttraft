package com.thzc.ttraft.core.log.dir;


import com.google.common.io.Files;
import com.thzc.ttraft.core.log.LogException;

import java.io.File;
import java.io.IOException;

abstract class AbstractLogDir implements LogDir {

    final File dir;

    AbstractLogDir(File dir) {
        this.dir = dir;
    }

    @Override
    public void initialize() {
        if (!dir.exists() && !dir.mkdir()) {
            throw new LogException("failed to create directory " + dir);
        }
        try {
            Files.touch(getEntriesFile());
            Files.touch(getEntryIndexFile());
        } catch (IOException e) {
            throw new LogException("failed to create file", e);
        }
    }

    @Override
    public boolean exists() {
        return dir.exists();
    }

    @Override
    public File getDir() {
        return dir;
    }

    @Override
    public boolean renameTo(LogDir logDir) {
        return dir.renameTo(logDir.getDir());
    }

    @Override
    public File getEntriesFile() {
        return new File(dir, RootDir.FILE_NAME_ENTRIES);
    }

    @Override
    public File getEntryIndexFile() {
        return new File(dir, RootDir.FILE_NAME_ENTRY_INDEX);
    }

}
