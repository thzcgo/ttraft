package com.thzc.ttraft.kv.client.command;

import com.thzc.ttraft.kv.client.CommandContext;

public class ClientRemoveServerCommand implements Command {

    @Override
    public String getName() {
        return "client-remove-server";
    }

    @Override
    public void execute(String arguments, CommandContext context) {
        if (arguments.isEmpty()) {
            throw new IllegalArgumentException("usage: " + getName() + " <node-id>");
        }

        if (context.clientRemoveServer(arguments)) {
            context.printSeverList();
        } else {
            System.err.println("no such server [" + arguments + "]");
        }
    }

}
