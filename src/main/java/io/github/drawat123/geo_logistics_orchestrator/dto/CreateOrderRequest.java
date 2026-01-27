package io.github.drawat123.geo_logistics_orchestrator.dto;

import io.github.drawat123.geo_logistics_orchestrator.model.Order;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderRequest(@NotNull @DecimalMin("0.01") BigDecimal orderValue,
                                 @NotNull double destinationLat,
                                 @NotNull double destinationLon) {
    public static Order toEntity(CreateOrderRequest request) {
        Order order = new Order();
        order.setOrderValue(request.orderValue());
        order.setDestinationLat(request.destinationLat());
        order.setDestinationLon(request.destinationLon());
        return order;
    }
}
