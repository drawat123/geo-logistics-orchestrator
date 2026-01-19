package io.github.drawat123.geo_logistics_orchestrator.graph.service;

import io.github.drawat123.geo_logistics_orchestrator.graph.model.LocationNode;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.RoadEdge;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CityGraphService {
    void addLocation(LocationNode node);

    // Connects sourceId -> targetId with a specific weight
    void addRoad(String sourceId, String targetId, double weight);

    // Returns the list of outgoing roads from a specific location
    List<RoadEdge> getAdjacencyList(String nodeId);

    LocationNode getNode(String nodeId);

    // Check if a node exists
    boolean containsNode(String nodeId);

    LocationNode findNearestNode(double lat, double lon);
}