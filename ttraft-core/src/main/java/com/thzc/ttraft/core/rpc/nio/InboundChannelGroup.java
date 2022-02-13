package com.thzc.ttraft.core.rpc.nio;

import com.thzc.ttraft.core.node.NodeId;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InboundChannelGroup {

    private static final Logger logger = LoggerFactory.getLogger(InboundChannelGroup.class);
    private final List<NioChannel> channels = new CopyOnWriteArrayList<>();

    public void add(NodeId remoteId, NioChannel channel) {
        logger.debug("channel INBOUND-{} connected", remoteId);
        channel.getNettyChannel().closeFuture().addListener(
                (ChannelFutureListener) future -> {
                    logger.debug("channel INBOUND-{} disconnected", remoteId);
                    remove(channel);
                }
        );
    }

    public void remove(NioChannel channel) {
        channels.remove(channel);
    }

    void closeAll() {
        logger.debug("close all inbound channels");
        for (NioChannel channel : channels) {
            channel.close();
        }
    }
}
