package com.thzc.ttraft.kv.client;

import com.thzc.ttraft.core.service.ServerRouter;
import com.thzc.ttraft.kv.server.message.AddNodeCommand;
import com.thzc.ttraft.kv.server.message.GetCommand;
import com.thzc.ttraft.kv.server.message.RemoveNodeCommand;
import com.thzc.ttraft.kv.server.message.SetCommand;

public class Client {

    public static final String VERSION = "1.0";

    private final ServerRouter serverRouter;

    public Client(ServerRouter serverRouter) {
        this.serverRouter = serverRouter;
    }

    public ServerRouter getServerRouter() {
        return serverRouter;
    }

    public void addNote(String nodeId, String host, int port) {
        serverRouter.send(new AddNodeCommand(nodeId, host, port));
    }

    public void removeNode(String nodeId) {
        serverRouter.send(new RemoveNodeCommand(nodeId));
    }

    public void set(String key, byte[] value) {
        serverRouter.send(new SetCommand(key, value));
    }

    public byte[] get(String key) {
        return (byte[]) serverRouter.send(new GetCommand(key));
    }
}
