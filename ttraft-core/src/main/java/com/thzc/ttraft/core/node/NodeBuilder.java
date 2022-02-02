package com.thzc.ttraft.core.node;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.thzc.ttraft.core.log.FileLog;
import com.thzc.ttraft.core.log.Log;
import com.thzc.ttraft.core.log.MemoryLog;
import com.thzc.ttraft.core.node.config.NodeConfig;
import com.thzc.ttraft.core.node.store.FileNodeStore;
import com.thzc.ttraft.core.node.store.MemoryNodeStore;
import com.thzc.ttraft.core.node.store.NodeStore;
import com.thzc.ttraft.core.rpc.Connector;
import com.thzc.ttraft.core.rpc.nio.NioConnector;
import com.thzc.ttraft.core.schedule.DefaultScheduler;
import com.thzc.ttraft.core.schedule.Scheduler;
import com.thzc.ttraft.core.support.ListeningTaskExecutor;
import com.thzc.ttraft.core.support.TaskExecutor;
import io.netty.channel.nio.NioEventLoopGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;

public class NodeBuilder {

    private final NodeGroup group;
    private final NodeId selfId;
    private final EventBus eventBus;
    private NodeConfig config = new NodeConfig();
    private Log log = null;
    private NodeStore store = null;
    private Scheduler scheduler = null;
    private Connector connector = null;
    private TaskExecutor taskExecutor = null;
    private TaskExecutor groupConfigChangeTaskExecutor = null;

    private boolean standby = false;

    private NioEventLoopGroup workerNioEventLoopGroup = null;


    public NodeBuilder(@Nonnull NodeEndpoint endpoint) {
        this(Collections.singletonList(endpoint), endpoint.getId());
    }

    public NodeBuilder(@Nonnull Collection<NodeEndpoint> endpoints, @Nonnull NodeId selfId) {
        Preconditions.checkNotNull(endpoints);
        Preconditions.checkNotNull(selfId);
        this.group = new NodeGroup(endpoints, selfId);
        this.selfId = selfId;
        this.eventBus = new EventBus(selfId.getValue());
    }

//    @Deprecated
//    public NodeBuilder(@Nonnull NodeId selfId, @Nonnull NodeGroup group) {
//        Preconditions.checkNotNull(selfId);
//        Preconditions.checkNotNull(group);
//        this.selfId = selfId;
//        this.group = group;
//        this.eventBus = new EventBus(selfId.getValue());
//    }


    public NodeBuilder setStandby(boolean standby) {
        this.standby = standby;
        return this;
    }


    public NodeBuilder setConfig(@Nonnull NodeConfig config) {
        Preconditions.checkNotNull(config);
        this.config = config;
        return this;
    }

    NodeBuilder setConnector(@Nonnull Connector connector) {
        Preconditions.checkNotNull(connector);
        this.connector = connector;
        return this;
    }


    public NodeBuilder setWorkerNioEventLoopGroup(@Nonnull NioEventLoopGroup workerNioEventLoopGroup) {
        Preconditions.checkNotNull(workerNioEventLoopGroup);
        this.workerNioEventLoopGroup = workerNioEventLoopGroup;
        return this;
    }

    NodeBuilder setScheduler(@Nonnull Scheduler scheduler) {
        Preconditions.checkNotNull(scheduler);
        this.scheduler = scheduler;
        return this;
    }


    NodeBuilder setTaskExecutor(@Nonnull TaskExecutor taskExecutor) {
        Preconditions.checkNotNull(taskExecutor);
        this.taskExecutor = taskExecutor;
        return this;
    }

    NodeBuilder setGroupConfigChangeTaskExecutor(@Nonnull TaskExecutor groupConfigChangeTaskExecutor) {
        Preconditions.checkNotNull(groupConfigChangeTaskExecutor);
        this.groupConfigChangeTaskExecutor = groupConfigChangeTaskExecutor;
        return this;
    }

    NodeBuilder setStore(@Nonnull NodeStore store) {
        Preconditions.checkNotNull(store);
        this.store = store;
        return this;
    }

    public NodeBuilder setDataDir(@Nullable String dataDirPath) {
        if (dataDirPath == null || dataDirPath.isEmpty()) {
            return this;
        }
        File dataDir = new File(dataDirPath);
        if (!dataDir.isDirectory() || !dataDir.exists()) {
            throw new IllegalArgumentException("[" + dataDirPath + "] not a directory, or not exists");
        }
        log = new FileLog(dataDir);
        store = new FileNodeStore(new File(dataDir, FileNodeStore.FILE_NAME));
        return this;
    }

    @Nonnull
    public Node build() {
        return new NodeImpl(buildContext());
    }

    @Nonnull
    private NodeContext buildContext() {
        NodeContext context = new NodeContext();
        context.setGroup(group);
        context.setMode(evaluateMode());
        context.setLog(log != null ? log : new MemoryLog());
        context.setStore(store != null ? store : new MemoryNodeStore());
        context.setSelfId(selfId);
        context.setConfig(config);
        context.setEventBus(eventBus);
        context.setScheduler(scheduler != null ? scheduler : new DefaultScheduler(config));
        context.setConnector(connector != null ? connector : createNioConnector());
        context.setTaskExecutor(taskExecutor != null ? taskExecutor : new ListeningTaskExecutor(
                Executors.newSingleThreadExecutor(r -> new Thread(r, "node"))
        ));
        // TODO share monitor
        context.setGroupConfigChangeTaskExecutor(groupConfigChangeTaskExecutor != null ? groupConfigChangeTaskExecutor :
                new ListeningTaskExecutor(Executors.newSingleThreadExecutor(r -> new Thread(r, "group-config-change"))));
        return context;
    }

    @Nonnull
    private NioConnector createNioConnector() {
        int port = group.findSelf().getEndpoint().getAddress().getPort();
        if (workerNioEventLoopGroup != null) {
            return new NioConnector(workerNioEventLoopGroup, selfId, eventBus, port);
        }
        return new NioConnector(new NioEventLoopGroup(config.getNioWorkerThreads()), false, selfId, eventBus, port);
    }

    @Nonnull
    private NodeMode evaluateMode() {
        if (standby) {
            return NodeMode.STANDBY;
        }
        if (group.isStandalone()) {
            return NodeMode.STANDALONE;
        }
        return NodeMode.GROUP_MEMBER;
    }

}
