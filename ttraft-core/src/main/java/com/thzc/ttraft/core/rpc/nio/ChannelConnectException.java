package com.thzc.ttraft.core.rpc.nio;

public class ChannelConnectException extends ChannelException {

    public ChannelConnectException(Throwable cause) {
        super(cause);
    }

    public ChannelConnectException(String message, Throwable cause) {
        super(message, cause);
    }

}