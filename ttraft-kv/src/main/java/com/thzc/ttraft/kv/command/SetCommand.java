package com.thzc.ttraft.kv.command;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.thzc.ttraft.core.proto.Protos;
import com.thzc.ttraft.kv.proto.kvstore;

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

    public static SetCommand fromBytes(byte[] bytes) {
        try {
            kvstore.SetCommand setCommand = kvstore.SetCommand.parseFrom(bytes);
            return new SetCommand(setCommand.getRequestId(), setCommand.getKey(), setCommand.getValue().toByteArray());
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException("反序列化 set 命令 失败");
        }
    }

    public byte[] toBytes() {
        return kvstore.SetCommand.newBuilder().setRequestId(this.requestId)
                .setKey(this.key)
                .setValue(ByteString.copyFrom(this.value)).build().toByteArray();
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
