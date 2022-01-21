package com.thzc.ttraft.core.node.role;

import java.util.*;
import java.util.stream.Collectors;

/*
*  集群成员表
* */
public class NodeGroup {

    private final NodeId selfId;
    private Map<NodeId, GroupMember> memberMap;

    // 单节点构造函数
    public NodeGroup(NodeEndpoint endpoint) {
        this(Collections.singleton(endpoint), endpoint.getId());
    }

    // 多节点构造函数
    public NodeGroup(Collection<NodeEndpoint> endpoints, NodeId selfId) {
        this.memberMap = buildMemberMap(endpoints);
        this.selfId = selfId;
    }

    private Map<NodeId, GroupMember> buildMemberMap(Collection<NodeEndpoint> endpoints) {
        if (endpoints.isEmpty()) throw new IllegalArgumentException("节点集合为空");
        Map<NodeId, GroupMember> map = new HashMap<>();
        for (NodeEndpoint endpoint : endpoints) {
            map.put(endpoint.getId(), new GroupMember(endpoint));
        }
        return map;
    }

    // 按节点id查找成员，找不到抛错
    GroupMember findMember(NodeId id) {
        GroupMember member = memberMap.get(id);
        if (member == null) throw new IllegalArgumentException("节点id为空");
        return  member;
    }
    // 按节点id查找成员，找不到返回空
    GroupMember getMember(NodeId id) {
        return memberMap.get(id);
    }

    // 返回除自己的其他所有节点
    Collection<GroupMember> listReplicationTarget() {
        return memberMap.values().stream().filter(
                m -> m.idEquals(selfId)
        ).collect(Collectors.toList());
    }

    public Set<NodeEndpoint> listEndpointExceptSelf() {
        Set<NodeEndpoint> set = new HashSet<>();
        for (GroupMember member : memberMap.values()) {
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
        for (GroupMember member : memberMap.values()) {
            if (!member.idEquals(selfId)) matchIndices.add(new NodeMatchIndex(member.getId(), member.getMatchIndex()));
        }
        int count = matchIndices.size();
        if (count == 0) throw new IllegalStateException("无稳定节点");
        Collections.sort(matchIndices);
        return matchIndices.get(count/2).getMatchIndex();
    }

}
