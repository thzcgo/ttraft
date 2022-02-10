package com.thzc.ttraft.core.log.dir;

import java.io.File;

public interface LogDir {

    // 初始化目录
    void initialize();

    // 目录是否存在
    boolean exists();

    // 获取目录
    File getDir();

    // 重命名
    boolean renameTo(LogDir logDir);

    // 获取 EntriesFile 文件
    File getEntriesFile();

    // 获取 EntryIndexFile 文件
    File getEntryIndexFile();
}
