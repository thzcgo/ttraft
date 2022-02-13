package com.thzc.ttraft.core.rpc.nio;

import com.thzc.ttraft.core.rpc.Channel;
import com.thzc.ttraft.core.rpc.message.AppendEntriesResult;
import com.thzc.ttraft.core.rpc.message.AppendEntriesRpc;
import com.thzc.ttraft.core.rpc.message.RequestVoteResult;
import com.thzc.ttraft.core.rpc.message.RequestVoteRpc;
import io.netty.channel.ChannelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioChannel implements Channel {

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
