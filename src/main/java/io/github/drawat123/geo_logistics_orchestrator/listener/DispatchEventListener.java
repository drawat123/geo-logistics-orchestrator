package io.github.drawat123.geo_logistics_orchestrator.listener;

import io.github.drawat123.geo_logistics_orchestrator.dto.OrderCreatedEvent;
import io.github.drawat123.geo_logistics_orchestrator.service.DispatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DispatchEventListener {
    private final DispatchService dispatchService;

    public DispatchEventListener(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @Async // This runs in a background thread pool!
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.debug("Async processing for Order: {} [Thread: {}]", event.orderId(), Thread.currentThread().getName());

        try {
            // The heavy lifting logic (Day 3 & 4 code)
            dispatchService.assignDriverToOrder(event.orderId());
        } catch (Exception e) {
            // Since this is async, the Controller is already gone.
            // We must log errors here, otherwise they disappear silently.
            log.error("Failed to dispatch order {}: {}", event.orderId(), e.getMessage());
        }
    }
}