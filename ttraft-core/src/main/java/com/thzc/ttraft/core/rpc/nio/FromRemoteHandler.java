package com.thzc.ttraft.core.rpc.nio;

import com.google.common.eventbus.EventBus;
import com.thzc.ttraft.core.node.role.NodeId;
import io.netty.channel.ChannelHandlerContext;

public class FromRemoteHandler extends AbstractHandler{

    private final InboundChannelGroup channelGroup;

    public FromRemoteHandler(EventBus eventBus, InboundChannelGroup channelGroup) {
        super(eventBus);
        this.channelGroup = channelGroup;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof NodeId) {
            remoteId = (NodeId) msg;
            NioChannel nioChannel = new NioChannel(ctx.channel());
            channelGroup.add(remoteId, nioChannel);
            return;
        }
        super.channelRead(ctx, msg);
    }
}
