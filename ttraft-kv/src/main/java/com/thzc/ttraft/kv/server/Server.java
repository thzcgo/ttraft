package com.thzc.ttraft.kv.server;

import com.thzc.ttraft.core.node.role.Node;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

    private final Node node;
    private final int port;
    private final Service service;
    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final NioEventLoopGroup workGroup = new NioEventLoopGroup(4);

    public Server(Node node, int port) {
        this.node = node;
        this.port = port;
        this.service = new Service(node);
    }

    public void start() {
        this.node.start();
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new Encoder());
                        pipeline.addLast(new Decoder());
                        pipeline.addLast(new ServiceHandler(service));
                    }
                });
        serverBootstrap.bind(this.port);
    }

    public void stop() throws InterruptedException {
        this.node.stop();
        this.workGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
    }
}
