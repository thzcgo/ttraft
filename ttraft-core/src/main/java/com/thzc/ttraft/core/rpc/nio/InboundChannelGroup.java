package com.thzc.ttraft.core.rpc.nio;

import com.thzc.ttraft.core.node.role.NodeId;
import io.netty.channel.ChannelFutureListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InboundChannelGroup {

    private final List<NioChannel> channels = new CopyOnWriteArrayList<>();

    public void add(NodeId remoteId, NioChannel channel) {
        channel.getNettyChannel().closeFuture().addListener(
                (ChannelFutureListener) future -> {
                    remove(channel);
                }
        );
    }

    public void remove(NioChannel channel) {
        channels.remove(channel);
    }

    void closeAll() {
        for (NioChannel channel : channels) {
            channel.close();
        }
    }
}
