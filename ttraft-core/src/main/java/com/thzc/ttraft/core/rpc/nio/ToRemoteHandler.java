package com.thzc.ttraft.core.rpc.nio;

import com.google.common.eventbus.EventBus;
import com.thzc.ttraft.core.node.NodeId;
import io.netty.channel.ChannelHandlerContext;

public class ToRemoteHandler extends AbstractHandler {

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
}
