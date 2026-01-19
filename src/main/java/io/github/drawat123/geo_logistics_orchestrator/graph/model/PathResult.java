package io.github.drawat123.geo_logistics_orchestrator.graph.model;

import java.util.List;

public record PathResult(double totalDistance, List<LocationNode> path) {
}