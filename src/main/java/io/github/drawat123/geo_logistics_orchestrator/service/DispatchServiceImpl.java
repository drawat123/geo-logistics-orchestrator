package io.github.drawat123.geo_logistics_orchestrator.service;

import io.github.drawat123.geo_logistics_orchestrator.dto.DispatchResult;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.LocationNode;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.PathResult;
import io.github.drawat123.geo_logistics_orchestrator.graph.service.CityGraphService;
import io.github.drawat123.geo_logistics_orchestrator.graph.service.PathFinderService;
import io.github.drawat123.geo_logistics_orchestrator.model.Driver;
import io.github.drawat123.geo_logistics_orchestrator.model.DriverStatus;
import io.github.drawat123.geo_logistics_orchestrator.model.Order;
import io.github.drawat123.geo_logistics_orchestrator.model.OrderStatus;
import io.github.drawat123.geo_logistics_orchestrator.repository.DriverRepository;
import io.github.drawat123.geo_logistics_orchestrator.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.*;

@Slf4j
@Service
public class DispatchServiceImpl implements DispatchService {
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final CityGraphService cityGraphService;
    private final PathFinderService pathfinderService;
    // 1. Inject the class into itself (Lazy to avoid circular dependency errors)
    @Autowired
    @Lazy
    private DispatchService self;

    public DispatchServiceImpl(OrderRepository orderRepository, DriverRepository driverRepository, CityGraphService cityGraphService, PathFinderService pathfinderService) {
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
        this.cityGraphService = cityGraphService;
        this.pathfinderService = pathfinderService;
    }

    @Override
    public DispatchResult assignDriverToOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Validate Order State
        if (order.getDriver() != null) {
            throw new IllegalStateException("Order is already assigned to driver " + order.getDriver().getId());
        }

        LocationNode targetNode = cityGraphService.findNearestNode(order.getDestinationLat(), order.getDestinationLon());
        if (targetNode == null) {
            throw new IllegalStateException("Order location is outside the service area (No graph node found)");
        }

        // Find the Closest Driver (The "Competition" Loop)
        List<Map.Entry<Driver, PathResult>> candidates = new ArrayList<>();

        List<Driver> drivers = driverRepository.findDriversByStatus(DriverStatus.AVAILABLE);
        for (Driver driver : drivers) {
            try {
                LocationNode startNode = cityGraphService.findNearestNode(driver.getLatitude(), driver.getLongitude());

                // Calculate Path
                PathResult result = pathfinderService.findShortestPath(cityGraphService, startNode.id(), targetNode.id());

                candidates.add(new AbstractMap.SimpleEntry<>(driver, result));
            } catch (Exception e) {
                log.error("Driver {} cannot reach target: {}", driver.getId(), e.getMessage());
            }
        }

        candidates.sort(Comparator.comparingDouble(e -> e.getValue().totalDistance()));

        // The Retry Loop (The Fix)
        for (var entry : candidates) {
            Driver driverCandidate = entry.getKey();
            PathResult path = entry.getValue();

            try {
                // We call a helper method to attempt the write operation in a FRESH transaction
                // Note: We need to pass IDs, not Entity objects, to ensure fresh fetching in the new transaction
                return self.attemptBooking(orderId, driverCandidate.getId(), path);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.error("Race condition: Driver {} was taken. Trying next...", driverCandidate.getId());
            } catch (Exception e) {
                log.error("Unexpected error booking driver: {}", e.getMessage());
            }
        }

        throw new IllegalStateException("Unable to assign order. All reachable drivers were taken or unavailable.");
    }

    /**
     * Helper method to isolate the Transaction.
     * Propagation.REQUIRES_NEW ensures this runs in a separate transaction.
     * If it fails (rolls back), it does NOT kill the parent loop.
     */
    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public DispatchResult attemptBooking(UUID orderId, UUID driverId, PathResult path) {
        // Re-fetch entities inside this new transaction to ensure latest state
        Order order = orderRepository.findById(orderId).orElseThrow();
        Driver driver = driverRepository.findById(driverId).orElseThrow();

        // Double check status inside the lock boundary
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new ObjectOptimisticLockingFailureException(Driver.class, driverId);
        }

        // Double check order hasn't been stolen too
        if (order.getDriver() != null) {
            throw new IllegalStateException("Order already assigned!");
        }

        // Apply Updates
        driver.setStatus(DriverStatus.BUSY);
        order.setDriver(driver);
        order.setStatus(OrderStatus.ASSIGNED);

        driverRepository.save(driver); // @Version check happens here
        orderRepository.save(order);

        double etaMinutes = (path.totalDistance() / 40.0) * 60;
        return new DispatchResult(driver.getId(), path.totalDistance(), etaMinutes);
    }
}
