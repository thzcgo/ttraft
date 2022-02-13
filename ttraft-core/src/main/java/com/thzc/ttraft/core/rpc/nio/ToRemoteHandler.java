package com.thzc.ttraft.core.rpc.nio;

import com.google.common.eventbus.EventBus;
import com.thzc.ttraft.core.node.NodeId;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToRemoteHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(ToRemoteHandler.class);
    private final NodeId selfNodeId;

    public ToRemoteHandler(EventBus eventBus, NodeId removeId, NodeId selfNodeId) {
        super(eventBus);
        this.remoteId = removeId;
        this.selfNodeId = selfNodeId;
    }

    public void channelActive(ChannelHandlerContext ctx) {
        ctx.write(selfNodeId);
        channel = new NioChannel(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("receive {} from {}", msg, remoteId);
        super.channelRead(ctx, msg);
    }
}
