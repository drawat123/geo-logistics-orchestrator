package io.github.drawat123.geo_logistics_orchestrator.dto;

import java.util.UUID;

public record DispatchResult(UUID driverId, double distanceKm, double estimatedTimeMinutes) {
}