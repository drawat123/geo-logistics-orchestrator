package io.github.drawat123.geo_logistics_orchestrator.graph.service;

import io.github.drawat123.geo_logistics_orchestrator.graph.model.PathResult;

public interface PathFinderService {
    /**
     * @param graph       The city graph data source
     * @param startNodeId The UUID of the starting location (e.g., Driver location)
     * @param endNodeId   The UUID of the delivery target
     * @return PathResult or throws PathNotFoundException
     */
    PathResult findShortestPath(CityGraphService graph, String startNodeId, String endNodeId);
}