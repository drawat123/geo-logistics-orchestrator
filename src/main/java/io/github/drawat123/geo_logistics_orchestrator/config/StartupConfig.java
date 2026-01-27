package io.github.drawat123.geo_logistics_orchestrator.config;

import io.github.drawat123.geo_logistics_orchestrator.dto.RegisterRequest;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.LocationNode;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.PathResult;
import io.github.drawat123.geo_logistics_orchestrator.graph.service.CityGraphService;
import io.github.drawat123.geo_logistics_orchestrator.graph.service.PathFinderService;
import io.github.drawat123.geo_logistics_orchestrator.model.*;
import io.github.drawat123.geo_logistics_orchestrator.repository.DriverRepository;
import io.github.drawat123.geo_logistics_orchestrator.repository.OrderRepository;
import io.github.drawat123.geo_logistics_orchestrator.repository.RoleRepository;
import io.github.drawat123.geo_logistics_orchestrator.repository.UserRepository;
import io.github.drawat123.geo_logistics_orchestrator.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class StartupConfig {
    private final CityGraphService cityGraphService;

    private final PathFinderService pathfinderService;

    private final DriverRepository driverRepository;

    private final OrderRepository orderRepository;

    private final RoleRepository roleRepository;

    private final AuthService authService;

    public StartupConfig(CityGraphService cityGraphService, PathFinderService pathfinderService, DriverRepository driverRepository, OrderRepository orderRepository, RoleRepository roleRepository, AuthService authService) {
        this.cityGraphService = cityGraphService;
        this.pathfinderService = pathfinderService;
        this.driverRepository = driverRepository;
        this.orderRepository = orderRepository;
        this.roleRepository = roleRepository;
        this.authService = authService;
    }

    @Bean
    public CommandLineRunner myCommandLineRunner() {
        // The run method will be executed by Spring Boot automatically at application startup
        return args -> {
            // If the table is empty, add the roles
            if (roleRepository.count() == 0) {
                roleRepository.save(new Role(ERole.ROLE_CUSTOMER));
                roleRepository.save(new Role(ERole.ROLE_DRIVER));
                roleRepository.save(new Role(ERole.ROLE_ADMIN));
            }

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

            log.info("Path: {}", pathResult.path());
            log.info("Distance: {}", pathResult.totalDistance());

            // Checking caching
            LocationNode node = cityGraphService.findNearestNode(10.1, 74.1);
            node = cityGraphService.findNearestNode(10.1, 74.1);

            if (driverRepository.count() == 0) {
                RegisterRequest request = new RegisterRequest(
                        "test driver",
                        "testdriver@mail.com",
                        "Test@123",
                        ERole.ROLE_DRIVER // <--- HARDCODE THIS
                );

                // 2. Seed a Driver (Positioned close to Node A)
                Driver driver = authService.registerDriver(request);
                driver.setStatus(DriverStatus.AVAILABLE);
                driver.setLatitude(10.1);
                driver.setLongitude(74.1);
                driverRepository.save(driver);
            }

            // 3. Seed an Order (Destination close to Node E)
            /*Order order = new Order();
            order.setOrderValue(BigDecimal.valueOf(100.50));
            order.setStatus(OrderStatus.PENDING);
            // Slightly offset from Node E (81, 63)
            order.setDestinationLat(81.1);
            order.setDestinationLon(63.1);
            // NOTE: We need the ID later for the API call, so print it
            Order savedOrder = orderRepository.save(order);

            log.info(">>> TEST DATA READY <<<");
            log.info("Driver ID: {}", driver.getId()); // UUID
            log.info("Order ID: {}", savedOrder.getId()); // UUID*/
        };
    }
}
