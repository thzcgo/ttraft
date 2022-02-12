package com.thzc.ttraft.kv.server.message;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.thzc.ttraft.kv.server.message.proto.KVstore;

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

    // 字节数组反序列化为SetCommand对象
    public static SetCommand fromBytes(byte[] bytes) {
        try {
            KVstore.SetCommand setCommand = KVstore.SetCommand.parseFrom(bytes);
            return new SetCommand(setCommand.getRequestId(), setCommand.getKey(), setCommand.getValue().toByteArray());
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException("反序列化 set 命令 失败");
        }
    }

    // SetCommand对象序列化为字节数组
    public byte[] toBytes() {
        return KVstore.SetCommand.newBuilder().setRequestId(this.requestId)
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
