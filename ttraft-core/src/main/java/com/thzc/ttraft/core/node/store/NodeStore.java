package com.thzc.ttraft.core.node.store;

import com.thzc.ttraft.core.node.NodeId;

/*
*  节点的 currentTerm 和 votedFor 需要持久化，便于节点重启恢复
* */
public interface NodeStore {

    int getTerm();

    void setTerm(int term);

    NodeId getVotedFor();

    void setVotedFor(NodeId votedFor);

    void close();

}
