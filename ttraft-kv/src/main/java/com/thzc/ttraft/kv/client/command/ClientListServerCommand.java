package com.thzc.ttraft.kv.client.command;

import com.thzc.ttraft.kv.client.CommandContext;

public class ClientListServerCommand implements Command {

    @Override
    public String getName() {
        return "client-list-server";
    }

    @Override
    public void execute(String arguments, CommandContext context) {
        context.printSeverList();
    }

}
