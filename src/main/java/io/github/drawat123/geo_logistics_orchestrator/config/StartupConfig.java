package io.github.drawat123.geo_logistics_orchestrator.config;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class StartupConfig {
    private final CityGraphService cityGraphService;

    private final PathfinderService pathfinderService;

    private final DriverRepository driverRepository;

    private final OrderRepository orderRepository;

    public StartupConfig(CityGraphService cityGraphService, PathfinderService pathfinderService, DriverRepository driverRepository, OrderRepository orderRepository) {
        this.cityGraphService = cityGraphService;
        this.pathfinderService = pathfinderService;
        this.driverRepository = driverRepository;
        this.orderRepository = orderRepository;
    }

    @Bean
    public CommandLineRunner myCommandLineRunner() {
        // The run method will be executed by Spring Boot automatically at application startup
        return args -> {
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

            PathResult pathResult = pathfinderService.findShortestPath(cityGraphService, l1.id(), l4.id());

            System.out.println("Path: " + pathResult.path());
            System.out.println("Distance: " + pathResult.totalDistance());

            // 2. Seed a Driver (Positioned close to Node A)
            Driver driver = new Driver();
            driver.setStatus(DriverStatus.AVAILABLE);
            driver.setLatitude(10.1);
            driver.setLongitude(74.1);
            driverRepository.save(driver);

            // 3. Seed an Order (Destination close to Node E)
            Order order = new Order();
            order.setOrderValue(BigDecimal.valueOf(100.50));
            order.setStatus(OrderStatus.PENDING);
            // Slightly offset from Node E (81, 63)
            order.setDestinationLat(81.1);
            order.setDestinationLon(63.1);
            // NOTE: We need the ID later for the API call, so print it
            Order savedOrder = orderRepository.save(order);

            System.out.println(">>> TEST DATA READY <<<");
            System.out.println("Driver ID: " + driver.getId()); // UUID
            System.out.println("Order ID: " + savedOrder.getId()); // UUID
        };
    }
}
