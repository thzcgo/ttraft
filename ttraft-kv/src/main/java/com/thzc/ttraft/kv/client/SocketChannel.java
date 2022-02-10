package com.thzc.ttraft.kv.client;

import com.thzc.ttraft.core.service.Channel;

public class SocketChannel implements Channel {

    private final String host;
    private final int port;

    public SocketChannel(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Object send(Object payload) {
        return null;
    }
}
