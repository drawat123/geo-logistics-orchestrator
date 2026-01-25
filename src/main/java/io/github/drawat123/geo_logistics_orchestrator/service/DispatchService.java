package io.github.drawat123.geo_logistics_orchestrator.service;

import io.github.drawat123.geo_logistics_orchestrator.dto.DispatchResult;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.PathResult;

import java.util.UUID;

public interface DispatchService {
    DispatchResult assignDriverToOrder(UUID orderId);

    DispatchResult attemptBooking(UUID orderId, UUID driverId, PathResult path);
}
