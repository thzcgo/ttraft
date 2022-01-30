package com.thzc.ttraft.kv.server;

import com.thzc.ttraft.core.log.statemachine.AbstractSingleThreadStateMachine;
import com.thzc.ttraft.core.node.Node;
import com.thzc.ttraft.core.node.role.RoleName;
import com.thzc.ttraft.core.node.role.RoleNameAndLeaderId;
import com.thzc.ttraft.kv.command.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Service {

    private final Node node;

    private final ConcurrentMap<String, CommandRequest<?>> pendingCommands = new ConcurrentHashMap<>();

    private Map<String, byte[]> map = new HashMap<>();

    public Service(Node node) {
        this.node = node;
        this.node.registerStateMachine(new StateMachineImpl() {});
    }

    public void get(CommandRequest<GetCommand> getCommandCommandRequest) {
        String key = getCommandCommandRequest.getCommand().getKey();
        byte[] value = this.map.get(key);
        getCommandCommandRequest.reply(new GetCommandResponse(value));
    }

    public void set(CommandRequest<SetCommand> commandRequest) {
        Redirect redirect = checkLeadership();
        if (redirect != null) {
            commandRequest.reply(redirect);
            return;
        }

        SetCommand command = commandRequest.getCommand();
        this.pendingCommands.put(command.getRequestId(), commandRequest);
        commandRequest.addCloseListener(() -> pendingCommands.remove(command.getRequestId()));
        this.node.appendLog(command.toBytes());
    }

    private Redirect checkLeadership() {
        RoleNameAndLeaderId state = node.getRoleNameAndLeaderId();
        if (state.getRoleName() != RoleName.LEADER) {
            return new Redirect(state.getLeaderId());
        }
        return null;
    }

    private class StateMachineImpl  extends AbstractSingleThreadStateMachine {


        @Override
        protected void applyCommand(byte[] commandBytes) {
            // 恢复命令
            SetCommand command = SetCommand.fromBytes(commandBytes);
            // 修改数据
            map.put(command.getKey(), command.getValue());
            // 查找连接
            CommandRequest<?> commandRequest = pendingCommands.remove(command.getRequestId());
            if (commandRequest != null) {
                commandRequest.reply(Success.INSTANCE);
            }
        }
    }

}
