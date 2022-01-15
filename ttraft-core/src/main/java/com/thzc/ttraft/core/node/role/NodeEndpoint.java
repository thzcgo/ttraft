package com.thzc.ttraft.core.node.role;

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

    public static class Address {
        private final String host;
        private final int port;

        public Address(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}
