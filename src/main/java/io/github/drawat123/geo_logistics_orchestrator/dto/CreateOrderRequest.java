package io.github.drawat123.geo_logistics_orchestrator.dto;

import io.github.drawat123.geo_logistics_orchestrator.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @Schema(
                description = "Total monetary value of the order. Used for insurance calculation.",
                example = "1250.75",
                minimum = "0.01",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull @DecimalMin("0.01") BigDecimal orderValue,
        @Schema(description = "Latitude of the delivery destination", example = "12.9716")
        @NotNull double destinationLat,
        @Schema(description = "Longitude of the delivery destination", example = "77.5946")
        @NotNull double destinationLon) {
    public static Order toEntity(CreateOrderRequest request) {
        Order order = new Order();
        order.setOrderValue(request.orderValue());
        order.setDestinationLat(request.destinationLat());
        order.setDestinationLon(request.destinationLon());
        return order;
    }
}
