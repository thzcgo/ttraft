package com.thzc.ttraft.kv.client.command;

public class KVStoreGetCommand implements Command{

    @Override
    public String getName() {
        return "kvstore-get";
    }

    @Override
    public void execute(String arguments, CommandContext commandContext) {
        if (arguments.isEmpty()) throw new IllegalArgumentException("usage: " + getName() + " <key>");
        byte[] valueBytes;
        valueBytes = commandContext.getClient().get(arguments);
        if (valueBytes == null) {
            System.out.println("null");
        } else {
            System.out.println(new String(valueBytes));
        }

    }
}
