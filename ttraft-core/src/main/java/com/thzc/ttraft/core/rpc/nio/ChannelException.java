package com.thzc.ttraft.core.rpc.nio;

public class ChannelException extends RuntimeException {

    public ChannelException(Throwable cause) {
        super(cause);
    }

    public ChannelException(String message, Throwable cause) {
        super(message, cause);
    }

}
