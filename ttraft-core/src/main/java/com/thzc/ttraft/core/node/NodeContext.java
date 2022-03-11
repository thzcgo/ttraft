package com.thzc.ttraft.core.node;

import com.google.common.eventbus.EventBus;
import com.thzc.ttraft.core.log.Log;
import com.thzc.ttraft.core.node.config.NodeConfig;
import com.thzc.ttraft.core.node.store.NodeStore;
import com.thzc.ttraft.core.rpc.Connector;
import com.thzc.ttraft.core.schedule.Scheduler;
import com.thzc.ttraft.core.schedule.TaskExecutor;

public class NodeContext {

    private NodeId selfId;
    private NodeGroup group;
    private NodeStore store;
    private NodeMode mode;
    private NodeConfig config;
    private Log log;
    private Connector connector;
    private EventBus eventBus;
    private Scheduler scheduler;
    private TaskExecutor taskExecutor;


    public NodeId getSelfId() {
        return selfId;
    }

    public void setSelfId(NodeId selfId) {
        this.selfId = selfId;
    }

    public NodeGroup getGroup() {
        return group;
    }

    public void setGroup(NodeGroup group) {
        this.group = group;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public NodeStore getStore() {
        return store;
    }

    public void setStore(NodeStore store) {
        this.store = store;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public NodeMode getMode() {
        return mode;
    }

    public void setMode(NodeMode mode) {
        this.mode = mode;
    }

    public NodeConfig getConfig() {
        return config;
    }

    public void setConfig(NodeConfig config) {
        this.config = config;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

}