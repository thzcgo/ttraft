package com.thzc.ttraft.kv.client;

import com.thzc.ttraft.core.node.NodeId;
import com.thzc.ttraft.core.rpc.Address;
import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.Map;

public class ConsoleLauncher {

    private void execute(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("gc")
                .hasArgs()
                .argName("server-config")
                .required()
                .desc("group config, required. format: <server-config> <server-config>. " +
                        "format of server config: <node-id>,<host>,<port-service>. e.g A,localhost,8001 B,localhost,8011")
                .build());
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ttraft-kv-client [OPTION]...", options);
            return;
        }

        CommandLineParser parser = new DefaultParser();
        Map<NodeId, Address> serverMap;
        try {
            CommandLine commandLine = parser.parse(options, args);
            serverMap = parseGroupConfig(commandLine.getOptionValues("gc"));
        } catch (ParseException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return;
        }

        Console console = new Console(serverMap);
        console.start();
    }

    /*** 将 A,localhost,8001 B,localhost,8011 格式化成 Map<NodeId, Address>  */
    private Map<NodeId, Address> parseGroupConfig(String[] rawGroupConfig) {
        Map<NodeId, Address> serverMap = new HashMap<>();
        for (String rawServerConfig : rawGroupConfig) {
            String[] pieces = rawServerConfig.split(",");
            if (pieces.length != 3) {
                throw new IllegalArgumentException("illegal server config [" + rawServerConfig + "]");
            }
            String nodeId = pieces[0];
            String host = pieces[1];
            int port;
            try {
                port = Integer.parseInt(pieces[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("illegal port [" + pieces[2] + "]");
            }

            serverMap.put(new NodeId(nodeId), new Address(host, port));
        }
        return serverMap;
    }


    public static void main(String[] args) throws Exception {
        ConsoleLauncher launcher = new ConsoleLauncher();
        launcher.execute(args);
    }
}
