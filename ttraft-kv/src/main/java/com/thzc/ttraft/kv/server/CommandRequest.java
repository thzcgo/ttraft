package com.thzc.ttraft.kv.server;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

public class CommandRequest<T> {
    private final T command;
    private final Channel channel;

    public CommandRequest(T command, Channel channel) {
        this.command = command;
        this.channel = channel;
    }

    public T getCommand() {
        return command;
    }

    // 响应结果
    public void reply(Object response) {
        this.channel.writeAndFlush(response);
    }

    // 添加关闭时的监听器
    public void addCloseListener(Runnable runnable) {
        this.channel.closeFuture().addListener(
                (ChannelFutureListener) future -> runnable.run()
        );
    }
}
