package com.thzc.ttraft.kv.client.command;

import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.rpc.Address;
import com.thzc.ttraft.core.service.ServerRouter;
import com.thzc.ttraft.kv.client.Client;
import com.thzc.ttraft.kv.client.SocketChannel;

import java.util.Map;

public class CommandContext {

    private final Map<NodeId, Address> serverMap;

    private Client client;

    private boolean running = false;

    public CommandContext(Map<NodeId, Address> serverMap) {
        this.serverMap = serverMap;
        this.client = new Client(buildServerRouter(serverMap));
    }

    private ServerRouter buildServerRouter(Map<NodeId, Address> serverMap) {
        ServerRouter router = new ServerRouter();
        for (NodeId nodeId : serverMap.keySet()) {
            Address address = serverMap.get(nodeId);
            router.add(nodeId, new SocketChannel(address.getHost(), address.getPort()));
        }
        return router;
    }

    Client getClient() {
        return client;
    }

    NodeId getClientLeader() {
        return client.getServerRouter().getLeaderId();
    }

    void setClientLeader(NodeId nodeId) {
        client.getServerRouter().setLeaderId(nodeId);
    }

    void clientAddServer(String nodeId, String host, int portService) {
        serverMap.put(new NodeId(nodeId), new Address(host, portService));
        client = new Client(buildServerRouter(serverMap));
    }

    boolean clientRemoveServer(String nodeId) {
        Address address = serverMap.remove(new NodeId(nodeId));
        if (address != null) {
            client = new Client(buildServerRouter(serverMap));
            return true;
        }
        return false;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    public void printSeverList() {
        for (NodeId nodeId : serverMap.keySet()) {
            Address address = serverMap.get(nodeId);
            System.out.println(nodeId + "," + address.getHost() + "," + address.getPort());
        }
    }
}
