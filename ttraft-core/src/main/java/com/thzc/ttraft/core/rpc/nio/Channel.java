package com.thzc.ttraft.core.rpc.nio;

import com.thzc.ttraft.core.node.role.AppendEntriesResult;
import com.thzc.ttraft.core.node.role.AppendEntriesRpc;
import com.thzc.ttraft.core.node.role.RequestVoteResult;
import com.thzc.ttraft.core.node.role.RequestVoteRpc;

public interface Channel {

    void writeRequestVoteRpc(RequestVoteRpc rpc);

    void writeRequestVoteResult(RequestVoteResult result);

    void writeAppendEntriesRpc(AppendEntriesRpc rpc);

    void writeAppendEntriesResult(AppendEntriesResult result);

    void close();
}
