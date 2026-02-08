package io.github.drawat123.geo_logistics_orchestrator.controller;

import io.github.drawat123.geo_logistics_orchestrator.dto.CreateOrderRequest;
import io.github.drawat123.geo_logistics_orchestrator.dto.OrderCreatedEvent;
import io.github.drawat123.geo_logistics_orchestrator.dto.OrderDTO;
import io.github.drawat123.geo_logistics_orchestrator.model.Order;
import io.github.drawat123.geo_logistics_orchestrator.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public OrderController(OrderService orderService, ApplicationEventPublisher eventPublisher, SimpMessagingTemplate simpMessagingTemplate) {
        this.orderService = orderService;
        this.eventPublisher = eventPublisher;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Operation(
            summary = "Create a new delivery order",
            description = "<b>Role: CUSTOMER only.</b><br>" +
                    "Creates a new order, triggers the internal dispatch event, and broadcasts " +
                    "real-time updates to the Admin Dashboard via WebSockets (`/topic/admin/orders`).",
            security = @SecurityRequirement(name = "Bearer Authentication") // Matches the name in your OpenApiConfig
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data (e.g., missing destination coordinates)",
                    content = @Content // No body for errors
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have ROLE_CUSTOMER",
                    content = @Content
            )
    })
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest request, Principal principal) {
        // 1. Service handles conversion from Request DTO -> Entity
        Order savedOrder = orderService.createOrder(request, principal.getName());

        // 2. Trigger Internal Business Logic (Dispatch)
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getId()));

        // 3. Notify Frontend via WebSocket (Optimization: Could also be done inside the EventListener)
        simpMessagingTemplate.convertAndSendToUser(principal.getName(), "/queue/orders", OrderDTO.fromEntity(savedOrder));

        // Notify Admins separately
        simpMessagingTemplate.convertAndSend("/topic/admin/orders", OrderDTO.fromEntity(savedOrder));

        return ResponseEntity.status(HttpStatus.CREATED).body(OrderDTO.fromEntity(savedOrder));
    }

    /**
     * @PostAuthorize is powerful! It runs AFTER the method.
     * "returnObject" refers to the Order returned by the service.
     * Logic: "Allow access if user is ADMIN OR if the order belongs to the user"
     */
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @PostAuthorize("hasRole('ADMIN') or returnObject.body.userEmail == authentication.name")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable UUID orderId) {
        Order order = orderService.findOrderById(orderId);
        // Note: We return DTO immediately. The @PostAuthorize will inspect this return object.
        // *Ensure OrderDTO has a 'getUserEmail()' getter for the SpEL to work.*
        return ResponseEntity.ok(OrderDTO.fromEntity(order));
    }
}