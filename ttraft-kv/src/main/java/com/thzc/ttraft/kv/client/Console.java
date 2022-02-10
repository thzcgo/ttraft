package com.thzc.ttraft.kv.client;

import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.rpc.Address;
import com.thzc.ttraft.kv.client.command.*;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Console {

    private static final String PROMPT = "kvstore-client " + Client.VERSION + "> ";
    private final Map<String, Command> commandMap; // 持有所有命令
    private final CommandContext commandContext; // 持有命令上下文（集群成员表、客户端）
    private final LineReader reader;

    public Console(Map<NodeId, Address> serverMap) {
        commandContext = new CommandContext(serverMap);

        commandMap = buildCommandMap(Arrays.asList(
                new ExitCommand(),
                new ClientAddServerCommand(),
                new ClientRemoveServerCommand(),
                new ClientListServerCommand(),
                new ClientGetLeaderCommand(),
                new ClientSetLeaderCommand(),
                new KVStoreGetCommand(),
                new KVStoreSetCommand()
        ));
        ArgumentCompleter completer = new ArgumentCompleter(
                new StringsCompleter(commandMap.keySet()),
                new NullCompleter()
        );
        reader = LineReaderBuilder.builder()
                .completer(completer)
                .build();
    }

    private Map<String, Command> buildCommandMap(Collection<Command> commands) {
        Map<String, Command> commandMap = new HashMap<>();
        for (Command cmd : commands) {
            commandMap.put(cmd.getName(), cmd);
        }
        return commandMap;
    }

    void start() {
        commandContext.setRunning(true);
        showInfo();
        String line;
        // 在 commandContext.running = true 时，持续接收命令行操作，分发给相应命令执行
        while (commandContext.isRunning()) {
            try {
                line = reader.readLine(PROMPT);
                if (line.trim().isEmpty())
                    continue;
                dispatchCommand(line);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            } catch (EndOfFileException ignored) {
                break;
            }
        }
    }

    private void showInfo() {
        System.out.println("Welcome to ttRaft KVStore Shell\n");
        System.out.println("***********************************************");
        System.out.println("current server list: \n");
        commandContext.printSeverList();
        System.out.println("***********************************************");
    }

    /*
    *  \s+ 表示匹配任何多个空白字符，包括空格、换行符、回车符、制表符、换页符等
    * */
    private void dispatchCommand(String line) {
        String[] commandNameAndArguments = line.split("\\s+", 2);
        String commandName = commandNameAndArguments[0];
        Command command = commandMap.get(commandName);
        if (command == null) {
            throw new IllegalArgumentException("no such command [" + commandName + "]");
        }
        command.execute(commandNameAndArguments.length > 1 ? commandNameAndArguments[1] : "", commandContext);
    }

}
