package com.thzc.ttraft.core.rpc.nio;

import com.google.protobuf.MessageLite;
import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.rpc.message.MessageConstants;
import com.thzc.ttraft.core.rpc.message.RequestVoteRpc;
import com.thzc.ttraft.core.proto.Protos;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Encoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {

        if (msg instanceof NodeId) {
            this.writeMessage(byteBuf, MessageConstants.MSG_TYPE_NODE_ID,((NodeId)msg).getValue().getBytes());
        } else if (msg instanceof RequestVoteRpc) {
            RequestVoteRpc rpc = (RequestVoteRpc)msg;
            Protos.RequestVoteRpc protoRpc = Protos.RequestVoteRpc.newBuilder()
                    .setTerm(rpc.getTerm())
                    .setCandidateId(rpc.getCandidateId().getValue())
                    .setLastLogIndex(rpc.getLastLogIndex())
                    .setLastLogTerm(rpc.getLastLogTerm())
                    .build();
            this.writeMessage(byteBuf, MessageConstants.MSG_TYPE_REQUEST_VOTE_RPC, protoRpc);
        }
    }

    private void writeMessage(ByteBuf out, int messageType, MessageLite message) throws IOException {
        out.writeInt(messageType);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeDelimitedTo(outputStream);
    }

    private void writeMessage(ByteBuf out, int messageType, byte[] bytes) {
        out.writeInt(messageType);
        this.writeBytes(out, bytes);
    }


    private void writeBytes(ByteBuf out, byte[] bytes) {
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
