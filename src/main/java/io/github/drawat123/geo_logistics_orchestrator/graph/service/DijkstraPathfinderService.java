package io.github.drawat123.geo_logistics_orchestrator.graph.service;

import io.github.drawat123.geo_logistics_orchestrator.exception.PathNotFoundException;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.LocationNode;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.PathResult;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.RoadEdge;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DijkstraPathfinderService implements PathfinderService {
    @Override
    public PathResult findShortestPath(CityGraphService graph, String startNodeId, String endNodeId) {
        // 1. Validation
        if (!graph.containsNode(startNodeId) || !graph.containsNode(endNodeId)) {
            throw new IllegalStateException("Start or End node does not exist in the graph.");
        }

        // 2. Initialization
        // Stores the shortest known distance to each node (Infinite by default)
        Map<String, Double> distances = new HashMap<>();

        // Stores the path history: Key = Node, Value = Previous Node in best path
        // Used to backtrack and reconstruction the path at the end.
        Map<String, String> previousNodes = new HashMap<>();

        // Priority Queue (Min-Heap) to explore the node with the smallest distance first
        PriorityQueue<NodeWrapper> pq = new PriorityQueue<>(
                Comparator.comparingDouble(NodeWrapper::distance)
        );

        // Initialize start node
        distances.put(startNodeId, 0.0);
        pq.add(new NodeWrapper(startNodeId, 0.0));

        // 3. The Core Loop (BFS with Priority)
        while (!pq.isEmpty()) {
            // Get the node with the smallest tentative distance
            NodeWrapper current = pq.poll();
            String currentNodeId = current.nodeId();

            // Optimization: Skip stale nodes (nodes processed before we found a shorter path to them)
            if (current.distance() > distances.get(currentNodeId)) {
                continue;
            }

            // Optimization: If we pulled the target node, we are done!
            if (currentNodeId.equals(endNodeId)) {
                break;
            }

            // Explore neighbors
            List<RoadEdge> neighbors = graph.getAdjacencyList(currentNodeId);
            for (RoadEdge edge : neighbors) {
                String neighborId = edge.target().id();

                double newDist = distances.get(currentNodeId) + edge.weight();
                double currentKnownDist = distances.getOrDefault(neighborId, Double.MAX_VALUE);

                // RELAXATION STEP:
                // If we found a shorter path to the neighbor through current node...
                if (newDist < currentKnownDist) {
                    distances.put(neighborId, newDist);
                    previousNodes.put(neighborId, currentNodeId);
                    // Add to PQ.
                    pq.add(new NodeWrapper(neighborId, newDist));
                }
            }
        }

        // 4. Path Reconstruction (Backtracking)
        if (!distances.containsKey(endNodeId)) {
            throw new PathNotFoundException("No path found between " + startNodeId + " and " + endNodeId);
        }

        List<LocationNode> path = new LinkedList<>(); // LinkedList is efficient for adding to the front
        String step = endNodeId;

        // Traverse backwards from Target -> Start using the 'previousNodes' map
        while (step != null) {
            // We need to fetch the actual Node object from the graph service
            path.addFirst(graph.getNode(step));
            step = previousNodes.get(step);
        }

        return new PathResult(distances.get(endNodeId), path);
    }

    /**
     * Helper class for PriorityQueue.
     */
    private record NodeWrapper(String nodeId, double distance) {
    }
}