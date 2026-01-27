package io.github.drawat123.geo_logistics_orchestrator.service;

import io.github.drawat123.geo_logistics_orchestrator.dto.CreateOrderRequest;
import io.github.drawat123.geo_logistics_orchestrator.model.Order;
import io.github.drawat123.geo_logistics_orchestrator.model.OrderStatus;
import io.github.drawat123.geo_logistics_orchestrator.model.User;
import io.github.drawat123.geo_logistics_orchestrator.repository.OrderRepository;
import io.github.drawat123.geo_logistics_orchestrator.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Order createOrder(CreateOrderRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        Order order = CreateOrderRequest.toEntity(request);
        order.setStatus(OrderStatus.PENDING);
        order.setUser(user);

        return orderRepository.save(order);
    }

    @Override
    public Order findOrderById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
