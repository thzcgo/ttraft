package com.thzc.ttraft.core.node.role;

public interface Node {

    void start();

    void close() throws InterruptedException;
}
