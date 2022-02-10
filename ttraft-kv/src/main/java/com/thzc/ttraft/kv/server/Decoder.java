package com.thzc.ttraft.kv.server;

import com.thzc.ttraft.kv.proto.kvstore;
import com.thzc.ttraft.kv.server.command.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class Decoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 8) return;
        in.markReaderIndex();
        int messageType = in.readInt();
        int payLoadLength = in.readInt();
        if (in.readableBytes() < payLoadLength) {
            in.resetReaderIndex();
            return;
        }

        byte[] payload = new byte[payLoadLength];
        in.readBytes(payload);
        switch (messageType) {
            case MessageConstants.MSG_TYPE_SET_COMMAND:
                kvstore.SetCommand setCommand = kvstore.SetCommand.parseFrom(payload);
                out.add(new SetCommand(setCommand.getKey(), setCommand.getValue().toByteArray()));
                break;
            case MessageConstants.MSG_TYPE_SUCCESS:
                out.add(Success.INSTANCE);
                break;
            case MessageConstants.MSG_TYPE_FAILURE:
                kvstore.Failure failure = kvstore.Failure.parseFrom(payload);
                out.add(new Failure(failure.getErrorCode(), failure.getMessage()));
                break;
            case MessageConstants.MSG_TYPE_REDIRECT:
                kvstore.Redirect redirect = kvstore.Redirect.parseFrom(payload);
                out.add(new Redirect(redirect.getLeaderId()));
                break;
            case MessageConstants.MSG_TYPE_GET_COMMAND:
                kvstore.GetCommand getCommand = kvstore.GetCommand.parseFrom(payload);
                out.add(new GetCommand(getCommand.getKey()));
                break;
            case MessageConstants.MSG_TYPE_GET_COMMAND_RESPONSE:
                kvstore.GetCommandResponse getCommandResponse = kvstore.GetCommandResponse.parseFrom(payload);
                out.add(new GetCommandResponse(getCommandResponse.getFound(), getCommandResponse.getValue().toByteArray()));
                break;
            default:
                throw new IllegalStateException("消息类型异常");

        }
    }
}
