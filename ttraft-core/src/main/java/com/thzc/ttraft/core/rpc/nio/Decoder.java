package com.thzc.ttraft.core.rpc.nio;

import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.rpc.message.MessageConstants;
import com.thzc.ttraft.core.rpc.message.RequestVoteRpc;
import com.thzc.ttraft.core.proto.Protos;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class Decoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {

        int availableBytes = in.readableBytes();
        if (availableBytes < 8) return;
        in.markReaderIndex();
        int messageType = in.readInt();
        int paloadLength = in.readInt();
        // 半包状态
        if (in.readableBytes() < paloadLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] payload = new byte[paloadLength];
        in.readBytes(payload);
        // 根据消息类型反序列化
        switch (messageType) {
            case MessageConstants.MSG_TYPE_NODE_ID:
                out.add(new NodeId(new String(payload)));
                break;
            case MessageConstants.MSG_TYPE_REQUEST_VOTE_RPC:
                Protos.RequestVoteRpc parseRpc = Protos.RequestVoteRpc.parseFrom(payload);
                RequestVoteRpc rpc = new RequestVoteRpc();
                rpc.setTerm(parseRpc.getTerm());
                rpc.setCandidateId(new NodeId(parseRpc.getCandidateId()));
                rpc.setLastLogIndex(parseRpc.getLastLogTerm());
                rpc.setLastLogTerm(parseRpc.getLastLogTerm());
                out.add(rpc);
                break;
        }

    }
}
