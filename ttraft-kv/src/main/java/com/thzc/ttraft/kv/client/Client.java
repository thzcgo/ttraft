package com.thzc.ttraft.kv.client;

import com.thzc.ttraft.core.service.ServerRouter;
import com.thzc.ttraft.kv.command.AddNodeCommand;
import com.thzc.ttraft.kv.command.GetCommand;
import com.thzc.ttraft.kv.command.RemoveNodeCommand;
import com.thzc.ttraft.kv.command.SetCommand;

public class Client {

    public static final String VERSION = "1.0";

    private final ServerRouter serverRouter;

    public Client(ServerRouter serverRouter) {
        this.serverRouter = serverRouter;
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

    public ServerRouter getServerRouter() {
        return serverRouter;
    }
}
