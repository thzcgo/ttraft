package com.thzc.ttraft.core.rpc;

import com.thzc.ttraft.core.node.NodeEndpoint;
import com.thzc.ttraft.core.rpc.message.*;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface Connector {

    // 初始化连接器
    void initialize();

    // 重置连接
    void resetChannels();

    // 关闭连接器
    void close();



    void sendRequestVote(@Nonnull RequestVoteRpc rpc, @Nonnull Collection<NodeEndpoint> destinationEndpoints);

    void replyRequestVote(@Nonnull RequestVoteResult result, @Nonnull RequestVoteRpcMessage rpcMessage);

    void sendAppendEntries(@Nonnull AppendEntriesRpc rpc, @Nonnull NodeEndpoint destinationEndpoint);

    void replyAppendEntries(@Nonnull AppendEntriesResult result, @Nonnull AppendEntriesRpcMessage rpcMessage);

}
