package com.thzc.ttraft.core.rpc;

public class ChannelConnectException extends ChannelException {

    public ChannelConnectException(Throwable cause) {
        super(cause);
    }

    public ChannelConnectException(String message, Throwable cause) {
        super(message, cause);
    }

}