package com.thzc.ttraft.kv.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.thzc.ttraft.kv.command.*;
import com.thzc.ttraft.kv.proto.kvstore;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {
        if (msg instanceof SetCommand) {
            SetCommand command = (SetCommand) msg;
            kvstore.SetCommand setCommand = kvstore.SetCommand.newBuilder().setKey(command.getKey()).setValue(ByteString.copyFrom(command.getValue())).build();
            this.writeMessage(MessageConstants.MSG_TYPE_SET_COMMAND, setCommand, byteBuf);
        } else if (msg instanceof Success) {
            this.writeMessage(MessageConstants.MSG_TYPE_SUCCESS, kvstore.Success.newBuilder().build(), byteBuf);
        } else if (msg instanceof Failure) {
            Failure failure = (Failure)msg;
            kvstore.Failure build = kvstore.Failure.newBuilder().setErrorCode(failure.getErrorCode()).setMessage(failure.getMessage()).build();
            this.writeMessage(MessageConstants.MSG_TYPE_FAILURE, build, byteBuf);
        } else if (msg instanceof Redirect) {
            Redirect redirect = (Redirect) msg;
            kvstore.Redirect build = kvstore.Redirect.newBuilder().setLeaderId(redirect.getLeaderId()).build();
            this.writeMessage(MessageConstants.MSG_TYPE_REDIRECT, build, byteBuf);
        } else if (msg instanceof GetCommand) {
            GetCommand getCommand = (GetCommand) msg;
            kvstore.GetCommand build = kvstore.GetCommand.newBuilder().setKey(getCommand.getKey()).build();
            this.writeMessage(MessageConstants.MSG_TYPE_GET_COMMAND, build, byteBuf);
        } else if (msg instanceof  GetCommandResponse) {
            GetCommandResponse getCommandResponse = (GetCommandResponse) msg;
            byte[] bytes = getCommandResponse.getValue();
            kvstore.GetCommandResponse build = kvstore.GetCommandResponse.newBuilder().setFound(getCommandResponse.isFound()).setValue(bytes != null ? ByteString.copyFrom(bytes) : ByteString.EMPTY).build();
            this.writeMessage(MessageConstants.MSG_TYPE_GET_COMMAND_RESPONSE, build, byteBuf);
        }
    }

    private void writeMessage(int messageType, MessageLite message, ByteBuf out) {
        out.writeInt(messageType);
        byte[] bytes = message.toByteArray();
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
