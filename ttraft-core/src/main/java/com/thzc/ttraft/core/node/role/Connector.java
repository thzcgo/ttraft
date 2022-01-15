package com.thzc.ttraft.core.node.role;

import java.util.Collection;

public interface Connector {

    // 初始化连接器
    void initialize();

    // 关闭连接器
    void close();

    // 发送 RequestVoteRpc 给多节点
    void sendRequestVote(RequestVoteRpc rpc, Collection<NodeEndpoint> destEndpoints);

    // follower 回复 RequestVoteResult 给candidate
    void replyRequestVote(RequestVoteResult result, NodeEndpoint destEndpoint);

    // 发送 AppendEntriesRpc 给单节点
    void sendAppendEntries(AppendEntriesRpc rpc, NodeEndpoint destEndpoint);

    // 回复 AppendEntriesResult 给单节点
    void replyAppendEntries(AppendEntriesResult result, NodeEndpoint destEndpoint);

}
