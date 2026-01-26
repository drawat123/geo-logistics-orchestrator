package io.github.drawat123.geo_logistics_orchestrator;

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
import io.github.drawat123.geo_logistics_orchestrator.service.DispatchService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootTest
public class DispatchConcurrencyTest {
    @Autowired
    DispatchService dispatchService;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    CityGraphService cityGraphService;

    @Test
    public void testConcurrentDispatch() throws InterruptedException {
        // Setup Data: 1 Driver, 2 Orders at different locations
        /*
            A(10,74) -> [( B(11,34), 5 ), ( C(8,10), 2 )]
            B(11,34) -> [( D(48,30), 4 )]
            C(8,10) -> [(D(48,30), 6), (E(81,63), 3)]
        */
        LocationNode l1 = new LocationNode("A", 10, 74);
        LocationNode l2 = new LocationNode("B", 11, 34);
        LocationNode l3 = new LocationNode("C", 8, 10);
        LocationNode l4 = new LocationNode("D", 48, 30);
        LocationNode l5 = new LocationNode("E", 81, 63);

        cityGraphService.addLocation(l1);
        cityGraphService.addLocation(l2);
        cityGraphService.addLocation(l3);
        cityGraphService.addLocation(l4);
        cityGraphService.addLocation(l5);

        cityGraphService.addRoad(l1.id(), l2.id(), 5);
        cityGraphService.addRoad(l1.id(), l3.id(), 2);
        cityGraphService.addRoad(l2.id(), l4.id(), 4);
        cityGraphService.addRoad(l3.id(), l4.id(), 6);
        cityGraphService.addRoad(l3.id(), l5.id(), 3);

        // Seed a Driver (Positioned close to Node A)
        Driver driver = new Driver();
        driver.setStatus(DriverStatus.AVAILABLE);
        driver.setLatitude(10.1);
        driver.setLongitude(74.1);
        Driver savedDriver = driverRepository.save(driver);

        // Seed Order1 (Destination close to Node E)
        Order order1 = new Order();
        order1.setOrderValue(BigDecimal.valueOf(100.50));
        order1.setStatus(OrderStatus.PENDING);
        // Slightly offset from Node E (81, 63)
        order1.setDestinationLat(81.1);
        order1.setDestinationLon(63.1);
        // NOTE: We need the ID later for the API call, so print it
        Order savedOrder1 = orderRepository.save(order1);

        // Seed Order2 (Destination close to Node D)
        Order order2 = new Order();
        order2.setOrderValue(BigDecimal.valueOf(50.10));
        order2.setStatus(OrderStatus.PENDING);
        // Slightly offset from Node S (81, 63)
        order2.setDestinationLat(48.1);
        order2.setDestinationLon(30.1);
        // NOTE: We need the ID later for the API call, so print it
        Order savedOrder2 = orderRepository.save(order2);

        // Run Threads
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        Order finalSavedOrder = savedOrder1;
        Runnable task1 = () -> {
            try {
                dispatchService.assignDriverToOrder(finalSavedOrder.getId());
            } catch (Exception e) {
                log.error("Task 1 failed: {}", e.getClass().getName());
            } finally {
                latch.countDown();
            }
        };

        // ... create task2 ...
        Order finalSavedOrder1 = savedOrder2;
        Runnable task2 = () -> {
            try {
                dispatchService.assignDriverToOrder(finalSavedOrder1.getId());
            } catch (Exception e) {
                log.error("Task 2 failed: {}", e.getClass().getName());
            } finally {
                latch.countDown();
            }
        };

        executor.submit(task1);
        executor.submit(task2);
        latch.await(); // Wait for both to finish

        // 3. Assertions
        // Verify that Driver is BUSY
        // Verify only 1 Order is ASSIGNED, the other might be UNASSIGNED or failed.
        savedDriver = driverRepository.findById(savedDriver.getId()).get();
        Assert.isTrue(savedDriver.getStatus() == DriverStatus.BUSY, "Driver is busy");

        savedOrder1 = orderRepository.findById(savedOrder1.getId()).get();
        Assert.isTrue(savedOrder1.getStatus() == OrderStatus.ASSIGNED, "Order1 assigned");

        savedOrder2 = orderRepository.findById(savedOrder2.getId()).get();
        Assert.isTrue(savedOrder2.getStatus() == OrderStatus.PENDING, "Order2 still pending");
    }
}