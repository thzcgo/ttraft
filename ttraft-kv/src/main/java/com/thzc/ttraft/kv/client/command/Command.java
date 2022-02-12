package com.thzc.ttraft.kv.client.command;

import com.thzc.ttraft.kv.client.CommandContext;

public interface Command {

    String getName();

    void execute(String arguments, CommandContext commandContext);
}
