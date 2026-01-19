package io.github.drawat123.geo_logistics_orchestrator.service;

import io.github.drawat123.geo_logistics_orchestrator.dto.DispatchResult;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.LocationNode;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.PathResult;
import io.github.drawat123.geo_logistics_orchestrator.graph.service.CityGraphService;
import io.github.drawat123.geo_logistics_orchestrator.graph.service.PathfinderService;
import io.github.drawat123.geo_logistics_orchestrator.model.Driver;
import io.github.drawat123.geo_logistics_orchestrator.model.DriverStatus;
import io.github.drawat123.geo_logistics_orchestrator.model.Order;
import io.github.drawat123.geo_logistics_orchestrator.model.OrderStatus;
import io.github.drawat123.geo_logistics_orchestrator.repository.DriverRepository;
import io.github.drawat123.geo_logistics_orchestrator.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DispatchServiceImpl implements DispatchService {
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final CityGraphService cityGraphService;
    private final PathfinderService pathfinderService;

    public DispatchServiceImpl(OrderRepository orderRepository, DriverRepository driverRepository, CityGraphService cityGraphService, PathfinderService pathfinderService) {
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
        this.cityGraphService = cityGraphService;
        this.pathfinderService = pathfinderService;
    }

    @Override
    @Transactional // Important! Ensures both Driver and Order are updated together.
    public DispatchResult assignDriverToOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // 1. Validate Order State
        if (order.getDriver() != null) {
            throw new IllegalStateException("Order is already assigned to driver " + order.getDriver().getId());
        }

        // 2. Find Available Drivers
        List<Driver> drivers = driverRepository.findDriversByStatus(DriverStatus.AVAILABLE);
        if (drivers.isEmpty()) {
            throw new IllegalStateException("No drivers available");
        }

        LocationNode targetNode = cityGraphService.findNearestNode(order.getDestinationLat(), order.getDestinationLon());
        if (targetNode == null) {
            throw new IllegalStateException("Order location is outside the service area (No graph node found)");
        }

        // 4. Find the Closest Driver (The "Competition" Loop)
        Driver selectedDriver = null;
        double minDistance = Double.MAX_VALUE;

        for (Driver driver : drivers) {
            try {
                LocationNode startNode = cityGraphService.findNearestNode(driver.getLatitude(), driver.getLongitude());

                // Calculate Path
                PathResult result = pathfinderService.findShortestPath(cityGraphService, startNode.id(), targetNode.id());

                // Is this driver closer than the previous best?
                if (result.totalDistance() < minDistance) {
                    minDistance = result.totalDistance();
                    selectedDriver = driver;
                }
            } catch (Exception e) {
                // FIX: If a specific driver cannot reach the target (disconnected graph),
                // log it and SKIP them. Don't crash the whole request.
                System.out.println("Driver " + driver.getId() + " cannot reach target: " + e.getMessage());
            }
        }

        if (selectedDriver == null) {
            throw new IllegalStateException("No reachable drivers found for this order location.");
        }

        // 5. Update Database (Persistence)
        // FIX: Link the order to the driver
        selectedDriver.setStatus(DriverStatus.BUSY);
        order.setDriver(selectedDriver);
        order.setStatus(OrderStatus.ASSIGNED);

        driverRepository.save(selectedDriver);
        orderRepository.save(order);

        // 6. Return Result (Minutes)
        double etaMinutes = (minDistance / 40.0) * 60;
        return new DispatchResult(selectedDriver.getId(), minDistance, etaMinutes);
    }
}
