package com.thzc.ttraft.core.node.role;

public interface Entry {

    int KIND_NO_OP = 0;
    int KIND_GENERAL = 1;

    int getKind();

    int getIndex();

    int getTerm();

    EntryMeta getMeta();

    byte[] getCommandBytes();

}
