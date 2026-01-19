package io.github.drawat123.geo_logistics_orchestrator.repository;

import io.github.drawat123.geo_logistics_orchestrator.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
