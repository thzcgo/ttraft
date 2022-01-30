package com.thzc.ttraft.core.node.store;

import java.io.IOException;


public class NodeStoreException extends RuntimeException {

    /**
     * Create.
     *
     * @param cause cause
     */
    public NodeStoreException(Throwable cause) {
        super(cause);
    }

    /**
     * Create.
     *
     * @param message message
     * @param cause cause
     */
    public NodeStoreException(String message, Throwable cause) {
        super(message, cause);
    }

}
