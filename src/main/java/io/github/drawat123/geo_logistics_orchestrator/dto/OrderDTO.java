package io.github.drawat123.geo_logistics_orchestrator.dto;

import io.github.drawat123.geo_logistics_orchestrator.model.Order;
import io.github.drawat123.geo_logistics_orchestrator.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderDTO(
        UUID id,
        BigDecimal orderValue,
        OrderStatus status,
        Double destinationLat,
        Double destinationLon,
        UUID driverId, // We only expose the ID, not the whole Driver object
        LocalDateTime createdAt
) {
    // Records allow you to add static helper methods inside them!
    public static OrderDTO fromEntity(Order order) {
        return new OrderDTO(
                order.getId(),
                order.getOrderValue(),
                order.getStatus(),
                order.getDestinationLat(),
                order.getDestinationLon(),
                (order.getDriver() != null) ? order.getDriver().getId() : null,
                order.getCreatedAt()
        );
    }
}
