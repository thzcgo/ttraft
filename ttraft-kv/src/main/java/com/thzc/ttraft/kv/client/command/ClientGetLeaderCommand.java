package com.thzc.ttraft.kv.client.command;

import com.thzc.ttraft.kv.client.CommandContext;

public class ClientGetLeaderCommand implements Command {

    @Override
    public String getName() {
        return "client-get-leader";
    }

    @Override
    public void execute(String arguments, CommandContext context) {
        System.out.println(context.getClientLeader());
    }

}
