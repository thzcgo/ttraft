package com.thzc.ttraft.core.node;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/*
*  集群成员表
* */
public class NodeGroup {

    private final NodeId selfId;
    private Map<NodeId, NodeGroupMember> memberMap;

    // 单节点构造函数
    public NodeGroup(NodeEndpoint endpoint) {
        this(Collections.singleton(endpoint), endpoint.getId());
    }

    // 多节点构造函数
    public NodeGroup(Collection<NodeEndpoint> endpoints, NodeId selfId) {
        this.memberMap = buildMemberMap(endpoints);
        this.selfId = selfId;
    }

    private Map<NodeId, NodeGroupMember> buildMemberMap(Collection<NodeEndpoint> endpoints) {
        if (endpoints.isEmpty()) throw new IllegalArgumentException("节点集合为空");
        Map<NodeId, NodeGroupMember> map = new HashMap<>();
        for (NodeEndpoint endpoint : endpoints) {
            map.put(endpoint.getId(), new NodeGroupMember(endpoint));
        }
        return map;
    }

    // 按节点id查找成员，找不到抛错
    NodeGroupMember findMember(NodeId id) {
        NodeGroupMember member = memberMap.get(id);
        if (member == null) throw new IllegalArgumentException("节点id为空");
        return  member;
    }
    // 按节点id查找成员，找不到返回空
    NodeGroupMember getMember(NodeId id) {
        return memberMap.get(id);
    }

    Collection<NodeGroupMember> listReplicationTarget() {
        return memberMap.values().stream().filter(
                m -> m.idEquals(selfId)
        ).collect(Collectors.toList());
    }

    // 返回除自己的其他所有节点
    public Set<NodeEndpoint> listEndpointExceptSelf() {
        Set<NodeEndpoint> set = new HashSet<>();
        for (NodeGroupMember member : memberMap.values()) {
            if (!member.getEndpoint().getId().equals(selfId)) {
                set.add(member.getEndpoint());
            }
        }
        return set;
    }

    public int getCount() {
        return memberMap.size();
    }

    // 计算过半commitIndex
    public int getMatchIndexOfMajor() {
        ArrayList<NodeMatchIndex> matchIndices = new ArrayList<>();
        for (NodeGroupMember member : memberMap.values()) {
            if (!member.idEquals(selfId)) matchIndices.add(new NodeMatchIndex(member.getId(), member.getMatchIndex()));
        }
        int count = matchIndices.size();
        if (count == 0) throw new IllegalStateException("无稳定节点");
        Collections.sort(matchIndices);
        return matchIndices.get(count/2).getMatchIndex();
    }

    boolean isStandalone() {
        return memberMap.size() == 1 && memberMap.containsKey(selfId);
    }

    @Nonnull
    NodeGroupMember findSelf() {
        return findMember(selfId);
    }

    public void resetReplicatingStates(int nextLogIndex) {
        for (NodeGroupMember member : memberMap.values()) {
            if (!member.idEquals(selfId)) {
                member.setReplicatingState(new ReplicatingState(nextLogIndex));
            }
        }
    }


    public static class NodeMatchIndex implements Comparable<NodeMatchIndex>{

        private final NodeId nodeId;
        private final int matchIndex;

        public NodeMatchIndex(NodeId nodeId, int matchIndex) {
            this.nodeId = nodeId;
            this.matchIndex = matchIndex;
        }

        public int getMatchIndex() {
            return matchIndex;
        }

        @Override
        public int compareTo(@Nonnull NodeMatchIndex o) {
            return Integer.compare(this.matchIndex, o.matchIndex);
        }

        @Override
        public String toString() {
            return "NodeMatchIndex{" +
                    "nodeId=" + nodeId +
                    ", matchIndex=" + matchIndex +
                    '}';
        }
    }

}
