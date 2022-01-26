package com.thzc.ttraft.kv.command;

import java.util.Arrays;
import java.util.UUID;

public class SetCommand {

    private final String requestId;
    private final String key;
    private final byte[] value;

    public SetCommand(String key, byte[] value) {
        this(UUID.randomUUID().toString(), key, value);
    }

    public SetCommand(String requestId, String key, byte[] value) {
        this.requestId = requestId;
        this.key = key;
        this.value = value;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SetCommand{" +
                "requestId='" + requestId + '\'' +
                ", key='" + key + '\'' +
                ", value=" + Arrays.toString(value) +
                '}';
    }
}
