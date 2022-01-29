package com.thzc.ttraft.kv.client.command;

public interface Command {

    String getName();

    void execute(String arguments, CommandContext commandContext);
}
