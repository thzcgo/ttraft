package com.thzc.ttraft.core.node;

import com.thzc.ttraft.core.rpc.Address;

public class NodeEndpoint {

    private final NodeId id;
    private final Address address;

    public NodeEndpoint(String id, String host, int port) {
        this(new NodeId(id), new Address(host, port));
    }

    public NodeEndpoint(NodeId id, Address address) {
        this.id = id;
        this.address = address;
    }

    public NodeId getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }


}
