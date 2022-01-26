package com.thzc.ttraft.core.rpc.nio;

import com.thzc.ttraft.core.node.role.AppendEntriesResult;
import com.thzc.ttraft.core.node.role.AppendEntriesRpc;
import com.thzc.ttraft.core.node.role.RequestVoteResult;
import com.thzc.ttraft.core.node.role.RequestVoteRpc;
import io.netty.channel.ChannelException;

public class NioChannel implements Channel{

    private final io.netty.channel.Channel nettyChannel;

    public NioChannel(io.netty.channel.Channel nettyChannel) {
        this.nettyChannel = nettyChannel;
    }

    @Override
    public void writeRequestVoteRpc(RequestVoteRpc rpc) {
        nettyChannel.writeAndFlush(rpc);
    }

    @Override
    public void writeRequestVoteResult(RequestVoteResult result) {
        nettyChannel.writeAndFlush(result);
    }

    @Override
    public void writeAppendEntriesRpc(AppendEntriesRpc rpc) {
        nettyChannel.writeAndFlush(rpc);
    }

    @Override
    public void writeAppendEntriesResult(AppendEntriesResult result) {
        nettyChannel.writeAndFlush(result);
    }

    @Override
    public void close() {
        try {
            nettyChannel.close().sync();
        } catch (InterruptedException e) {
            throw new ChannelException("关闭失败");
        }
    }

    public io.netty.channel.Channel getNettyChannel() {
        return nettyChannel;
    }
}
