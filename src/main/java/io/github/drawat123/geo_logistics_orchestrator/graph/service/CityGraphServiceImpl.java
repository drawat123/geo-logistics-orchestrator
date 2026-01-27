package io.github.drawat123.geo_logistics_orchestrator.graph.service;

import io.github.drawat123.geo_logistics_orchestrator.graph.model.LocationNode;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.RoadEdge;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CityGraphServiceImpl implements CityGraphService {

    // 1. Registry: ID -> Actual Node Object (for coordinates/data)
    private final Map<String, LocationNode> nodeRegistry = new ConcurrentHashMap<>();

    // 2. Adjacency List: ID -> Outgoing Edges
    private final Map<String, List<RoadEdge>> adjacencyList = new ConcurrentHashMap<>();

    @Override
    public void addLocation(LocationNode node) {
        nodeRegistry.put(node.id(), node);
        adjacencyList.putIfAbsent(node.id(), new ArrayList<>());
    }

    @Override
    public void addRoad(String sourceId, String targetId, double weight) {
        if (!nodeRegistry.containsKey(sourceId) || !nodeRegistry.containsKey(targetId)) {
            // Log warning: trying to connect non-existent nodes
            return;
        }

        // Get the actual target node object to store in the edge
        LocationNode targetNode = nodeRegistry.get(targetId);

        RoadEdge newEdge = new RoadEdge(targetNode, weight);
        adjacencyList.get(sourceId).add(newEdge);
    }

    @Override
    public List<RoadEdge> getAdjacencyList(String nodeId) {
        return adjacencyList.getOrDefault(nodeId, Collections.emptyList());
    }

    @Override
    public LocationNode getNode(String nodeId) {
        return nodeRegistry.get(nodeId);
    }

    @Override
    public boolean containsNode(String nodeId) {
        return nodeRegistry.containsKey(nodeId);
    }

    @Override
    @Cacheable(value = "nearestNode", key = "#lat + '-' + #lon")
    public LocationNode findNearestNode(double lat, double lon) {
        LocationNode inputNode = new LocationNode("", lat, lon);

        LocationNode nearestNode = nodeRegistry.values().stream()
                .min(Comparator.comparingDouble(node -> node.distanceTo(inputNode)))
                .orElse(null);

        return nearestNode;
    }
}