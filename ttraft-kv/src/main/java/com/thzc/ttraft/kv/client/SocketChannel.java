package com.thzc.ttraft.kv.client;


import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.service.Channel;
import com.thzc.ttraft.core.service.ChannelException;
import com.thzc.ttraft.core.service.RedirectException;
import com.thzc.ttraft.kv.server.message.proto.KVstore;
import com.thzc.ttraft.kv.server.message.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketChannel implements Channel {

    private final String host;
    private final int port;

    public SocketChannel(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Object send(Object payload) {
        try (Socket socket = new Socket()) {
            socket.setTcpNoDelay(true);
            socket.connect(new InetSocketAddress(this.host, this.port));
            this.write(socket.getOutputStream(), payload);
            return this.read(socket.getInputStream());
        } catch (IOException e) {
            throw new ChannelException("failed to send and receive", e);
        }
    }

    /*
    *  将待发送负载转型相应对象，再转为proto序列化格式，调用通用发送方法
    * */
    private void write(OutputStream output, Object payload) throws IOException {
        if (payload instanceof GetCommand) {
            KVstore.GetCommand protoGetCommand = KVstore.GetCommand.newBuilder().setKey(((GetCommand) payload).getKey()).build();
            this.write(output, MessageConstants.MSG_TYPE_GET_COMMAND, protoGetCommand);
        } else if (payload instanceof SetCommand) {
            SetCommand setCommand = (SetCommand) payload;
            KVstore.SetCommand protoSetCommand = KVstore.SetCommand.newBuilder()
                    .setKey(setCommand.getKey())
                    .setValue(ByteString.copyFrom(setCommand.getValue())).build();
            this.write(output, MessageConstants.MSG_TYPE_SET_COMMAND, protoSetCommand);
        } else if (payload instanceof AddNodeCommand) {
            AddNodeCommand command = (AddNodeCommand) payload;
            KVstore.AddNodeCommand protoAddServerCommand = KVstore.AddNodeCommand.newBuilder().setNodeId(command.getNodeId())
                    .setHost(command.getHost()).setPort(command.getPort()).build();
            this.write(output, MessageConstants.MSG_TYPE_ADD_SERVER_COMMAND, protoAddServerCommand);
        } else if (payload instanceof RemoveNodeCommand) {
            RemoveNodeCommand command = (RemoveNodeCommand) payload;
            KVstore.RemoveNodeCommand protoRemoveServerCommand = KVstore.RemoveNodeCommand.newBuilder().setNodeId(command.getNodeId().getValue()).build();
            this.write(output, MessageConstants.MSG_TYPE_REMOVE_SERVER_COMMAND, protoRemoveServerCommand);
        }
    }

    private void write(OutputStream output, int messageType, MessageLite message) throws IOException {
        DataOutputStream dataOutput = new DataOutputStream(output);
        byte[] messageBytes = message.toByteArray();
        dataOutput.writeInt(messageType);
        dataOutput.writeInt(messageBytes.length);
        dataOutput.write(messageBytes);
        dataOutput.flush();
    }

    /*
    *  响应
    * */
    private Object read(InputStream input) throws IOException {
        DataInputStream dataInput = new DataInputStream(input);
        int messageType = dataInput.readInt();
        int payloadLength = dataInput.readInt();
        byte[] payload = new byte[payloadLength];
        dataInput.readFully(payload);
        switch (messageType) {
            case MessageConstants.MSG_TYPE_SUCCESS:
                return null;
            case MessageConstants.MSG_TYPE_FAILURE:
                KVstore.Failure protoFailure = KVstore.Failure.parseFrom(payload);
                throw new ChannelException("error code " + protoFailure.getErrorCode() + ", message " + protoFailure.getMessage());
            case MessageConstants.MSG_TYPE_REDIRECT:
                KVstore.Redirect protoRedirect = KVstore.Redirect.parseFrom(payload);
                throw new RedirectException(new NodeId(protoRedirect.getLeaderId()));
            case MessageConstants.MSG_TYPE_GET_COMMAND_RESPONSE:
                KVstore.GetCommandResponse protoGetCommandResponse = KVstore.GetCommandResponse.parseFrom(payload);
                if (!protoGetCommandResponse.getFound()) return null;
                return protoGetCommandResponse.getValue().toByteArray();
            default:
                throw new ChannelException("unexpected message type " + messageType);
        }
    }
}
