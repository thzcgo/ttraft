package com.thzc.ttraft.kv.server;

import com.thzc.ttraft.kv.server.message.GetCommand;
import com.thzc.ttraft.kv.server.message.SetCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServiceHandler extends ChannelInboundHandlerAdapter {
    private final Service service;

    public ServiceHandler(Service service) {
        this.service = service;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof GetCommand) {
            service.get(new CommandRequest<>((GetCommand)msg, ctx.channel()));
        } else if (msg instanceof SetCommand) {
            service.set(new CommandRequest<>((SetCommand)msg, ctx.channel()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
