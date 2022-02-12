package com.thzc.ttraft.kv.client.command;

import com.thzc.ttraft.kv.client.CommandContext;

public class ExitCommand implements Command {

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public void execute(String arguments, CommandContext context) {
        System.out.println("bye");
        context.setRunning(false);
    }

}
