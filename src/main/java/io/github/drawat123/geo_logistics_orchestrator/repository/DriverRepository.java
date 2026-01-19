package io.github.drawat123.geo_logistics_orchestrator.repository;

import io.github.drawat123.geo_logistics_orchestrator.model.Driver;
import io.github.drawat123.geo_logistics_orchestrator.model.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {
    @Query("SELECT d FROM Driver d WHERE d.status = :status")
    List<Driver> findDriversByStatus(@Param("status") DriverStatus status);
}
