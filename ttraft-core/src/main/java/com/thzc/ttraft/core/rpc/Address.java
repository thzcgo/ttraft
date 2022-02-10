package com.thzc.ttraft.core.rpc;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Address {

    private final String host;
    private final int port;

    public Address(@Nonnull String host, int port) {
        Preconditions.checkNotNull(host);
        this.host = host;
        this.port = port;
    }

    @Nonnull
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Address{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

}
