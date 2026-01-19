package io.github.drawat123.geo_logistics_orchestrator.service;

import io.github.drawat123.geo_logistics_orchestrator.dto.DispatchResult;

import java.util.UUID;

public interface DispatchService {
    DispatchResult assignDriverToOrder(UUID orderId);
}
