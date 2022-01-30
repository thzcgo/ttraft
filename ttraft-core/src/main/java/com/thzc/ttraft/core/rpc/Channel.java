package com.thzc.ttraft.core.rpc;

import com.thzc.ttraft.core.rpc.message.AppendEntriesResult;
import com.thzc.ttraft.core.rpc.message.AppendEntriesRpc;
import com.thzc.ttraft.core.rpc.message.RequestVoteResult;
import com.thzc.ttraft.core.rpc.message.RequestVoteRpc;

public interface Channel {

    void writeRequestVoteRpc(RequestVoteRpc rpc);

    void writeRequestVoteResult(RequestVoteResult result);

    void writeAppendEntriesRpc(AppendEntriesRpc rpc);

    void writeAppendEntriesResult(AppendEntriesResult result);

    void close();
}
