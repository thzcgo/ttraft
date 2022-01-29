package com.thzc.ttraft.kv.client;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.thzc.ttraft.core.service.Channel;
import com.thzc.ttraft.core.service.ChannelException;
import com.thzc.ttraft.kv.command.MessageConstants;
import com.thzc.ttraft.kv.command.RemoveNodeCommand;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

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
