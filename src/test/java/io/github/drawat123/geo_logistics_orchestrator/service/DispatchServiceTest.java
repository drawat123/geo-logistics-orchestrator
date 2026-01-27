package io.github.drawat123.geo_logistics_orchestrator.service;

import io.github.drawat123.geo_logistics_orchestrator.dto.DispatchResult;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.LocationNode;
import io.github.drawat123.geo_logistics_orchestrator.graph.model.PathResult;
import io.github.drawat123.geo_logistics_orchestrator.graph.service.CityGraphService;
import io.github.drawat123.geo_logistics_orchestrator.graph.service.PathFinderService;
import io.github.drawat123.geo_logistics_orchestrator.model.*;
import io.github.drawat123.geo_logistics_orchestrator.repository.DriverRepository;
import io.github.drawat123.geo_logistics_orchestrator.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DispatchServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private CityGraphService cityGraphService; // Needed for nearest node
    @Mock
    private PathFinderService pathfinderService; // Needed for distance calc
    @Mock
    private SimpMessagingTemplate messagingTemplate; // Needed for notification

    @InjectMocks
    private DispatchServiceImpl dispatchService;

    private Order order;
    private Driver driverA; // 10km away
    private Driver driverB; // 20km away
    private LocationNode orderNode, driverNode;

    @BeforeEach
    void setUp() {
        // 1. Create a Dummy User
        User user = new User();
        user.setEmail("test@example.com"); // Required for notification
        user.setId(UUID.randomUUID());

        // 1. Setup Data
        order = new Order();
        order.setId(UUID.randomUUID());
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setDestinationLat(12.97);
        order.setDestinationLon(77.59);

        driverA = new Driver();
        driverA.setId(UUID.randomUUID());
        driverA.setStatus(DriverStatus.AVAILABLE);
        driverB = new Driver();
        driverB.setId(UUID.randomUUID());
        driverB.setStatus(DriverStatus.AVAILABLE);

        // 2. Setup Nodes (Mock Graph)
        orderNode = new LocationNode("A", 12.97, 77.59);
        driverNode = new LocationNode("B", 12.96, 77.58); // Dummy node for drivers

        // 3. IMPORTANT: Handle 'self' injection for the internal call
        // This makes 'self.attemptBooking' call the ACTUAL method in this test instance
        ReflectionTestUtils.setField(dispatchService, "self", dispatchService);
    }

    @Test
    void testAssignDriver_RaceCondition_FailsOverToNextDriver() {
        // --- STEP 1: Define Behavior (The Script) ---

        // A. Initial Fetch
        // Add this "Factory" that creates a FRESH CLONE every time
        when(orderRepository.findById(any())).thenAnswer(invocation -> {
            // 1. Create a NEW OBJECT in memory
            Order freshOrder = new Order();

            // 2. Copy the ID and Basic Info from your global test variable
            freshOrder.setId(order.getId());
            freshOrder.setUser(order.getUser());
            freshOrder.setStatus(OrderStatus.PENDING);
            freshOrder.setDestinationLat(12.97);
            freshOrder.setDestinationLon(77.59);

            // 3. CRITICAL: Explicitly set Driver to NULL
            freshOrder.setDriver(null);

            return Optional.of(freshOrder);
        });

        // B. Finding Candidates
        // Use doReturn() to prevent "WrongTypeOfReturnValue" errors
        doReturn(List.of(driverA, driverB))
                .when(driverRepository).findDriversByStatus(DriverStatus.AVAILABLE);

        // C. Pathfinding Mocks (Make Driver A closer than Driver B)
        when(cityGraphService.findNearestNode(anyDouble(), anyDouble())).thenReturn(orderNode);

        // Return path: Driver A (Distance 10), Driver B (Distance 20)
        // Logic: First call returns 10km, Second call returns 20km
        doReturn(new PathResult(10, List.of()))
                .doReturn(new PathResult(20, List.of()))
                .when(pathfinderService).findShortestPath(any(), eq(orderNode.id()), eq(orderNode.id()));

        // --- THE CORE LOGIC MOCKS ---

        // D. Inside attemptBooking(): Re-fetching entities
        // We need these because attemptBooking calls findById again!
        when(driverRepository.findById(driverA.getId())).thenReturn(Optional.of(driverA));
        when(driverRepository.findById(driverB.getId())).thenReturn(Optional.of(driverB));

        // E. The "Race Condition" Simulation
        // When we try to save Driver A -> FAIL (Optimistic Lock)
        doThrow(new ObjectOptimisticLockingFailureException(Driver.class, driverA.getId()))
                .when(driverRepository).save(driverA);

        // When we try to save Driver B -> SUCCESS
        when(driverRepository.save(driverB)).thenReturn(driverB);

        // --- STEP 2: Execute ---
        DispatchResult result = dispatchService.assignDriverToOrder(order.getId());

        // --- STEP 3: Verify ---

        // 1. Assert we got Driver B (the second one)
        assertEquals(driverB.getId(), result.driverId());

        // 2. Verify we actually tried to save Driver A first
        verify(driverRepository).save(driverA);

        // 3. Verify we successfully saved Driver B
        verify(driverRepository).save(driverB);

        // 3. Verify Order Save (The Critical Part)
        // We need to capture the object that was passed to save()
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        // Now assert on the CAPTURED object, not the local one
        assertNotNull(savedOrder.getDriver());
        assertEquals(driverB.getId(), savedOrder.getDriver().getId());
        assertEquals(OrderStatus.ASSIGNED, savedOrder.getStatus());
    }
}
