package io.github.drawat123.geo_logistics_orchestrator.controller;

import io.github.drawat123.geo_logistics_orchestrator.dto.OrderCreatedEvent;
import io.github.drawat123.geo_logistics_orchestrator.model.Order;
import io.github.drawat123.geo_logistics_orchestrator.model.OrderStatus;
import io.github.drawat123.geo_logistics_orchestrator.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher; // <--- Inject this

    public OrderController(OrderRepository orderRepository, ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        // 1. Save the Order (Fast)
        order.setStatus(OrderStatus.PENDING);
        Order savedOrder = orderRepository.save(order);

        // 2. Publish Event (Fire and Forget)
        // This triggers the DispatchEventListener in the background
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getId()));

        // 3. Return Immediately to User
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }
}