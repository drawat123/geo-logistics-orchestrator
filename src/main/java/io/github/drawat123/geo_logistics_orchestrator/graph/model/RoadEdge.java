package io.github.drawat123.geo_logistics_orchestrator.graph.model;

public record RoadEdge(LocationNode target, double weight) {
    // Simple POJO/Record to hold the destination and cost
}