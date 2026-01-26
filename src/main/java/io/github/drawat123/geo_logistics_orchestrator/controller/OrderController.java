package io.github.drawat123.geo_logistics_orchestrator.controller;

import io.github.drawat123.geo_logistics_orchestrator.dto.OrderCreatedEvent;
import io.github.drawat123.geo_logistics_orchestrator.dto.OrderDTO;
import io.github.drawat123.geo_logistics_orchestrator.model.Order;
import io.github.drawat123.geo_logistics_orchestrator.model.OrderStatus;
import io.github.drawat123.geo_logistics_orchestrator.repository.OrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

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
    public ResponseEntity<OrderDTO> createOrder(@RequestBody Order order) {
        // 1. Save the Order (Fast)
        order.setStatus(OrderStatus.PENDING);
        Order savedOrder = orderRepository.save(order);

        // 2. Publish Event (Fire and Forget)
        // This triggers the DispatchEventListener in the background
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getId()));

        // 3. Return Immediately to User
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderDTO.fromEntity(savedOrder));
    }

    @CrossOrigin(origins = "*") // For development only
    @GetMapping("/{orderId}")
    public OrderDTO getOrder(@PathVariable String orderId) {
        Optional<Order> order = orderRepository.findById(UUID.fromString(orderId));
        return order.map(OrderDTO::fromEntity).orElse(null);
    }
}