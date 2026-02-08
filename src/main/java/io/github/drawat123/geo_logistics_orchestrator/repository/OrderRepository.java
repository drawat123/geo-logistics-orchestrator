package io.github.drawat123.geo_logistics_orchestrator.repository;

import io.github.drawat123.geo_logistics_orchestrator.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    // Solution 1: JPQL with JOIN FETCH
    @Query("SELECT o FROM Order o JOIN FETCH o.driver")
    List<Order> findAllWithDrivers();

    // Solution 2: EntityGraph (cleaner)
    @EntityGraph(attributePaths = {"driver", "user"})
    List<Order> findAll();
}
