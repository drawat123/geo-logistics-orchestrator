package io.github.drawat123.geo_logistics_orchestrator.service;

import io.github.drawat123.geo_logistics_orchestrator.dto.CreateOrderRequest;
import io.github.drawat123.geo_logistics_orchestrator.model.Order;

import java.util.UUID;

public interface OrderService {
    Order createOrder(CreateOrderRequest request, String email);

    Order findOrderById(UUID id);
}
