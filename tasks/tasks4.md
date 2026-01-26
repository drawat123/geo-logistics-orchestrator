## **Day 4: Concurrency & Event-Driven Architecture**

**Objective:** Make the system resilient to race conditions and decouple the heavy "Pathfinding" logic from the
user-facing API.

### **Task 4.1: Break the System (The Concurrency Test)**

Before we fix it, we must prove it is broken. We will write a test that simulates a race condition.

**Requirements:**

1. Create a test class `DispatchConcurrencyTest`.
2. Setup: 1 Driver, 2 Orders.
3. Action: Use `ExecutorService` to trigger `assignDriverToOrder` for both orders simultaneously (in separate threads).
4. **Expected Result (Currently):** One succeeds, one fails with `ObjectOptimisticLockingFailureException`.

**Skeleton:**

```java

@Slf4j
@SpringBootTest
public class DispatchConcurrencyTest {

    @Autowired
    DispatchService dispatchService;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void testConcurrentDispatch() throws InterruptedException {
        // 1. Setup Data: 1 Driver, 2 Orders at different locations
        // ... seed data ...

        // 2. Run Threads
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        Runnable task1 = () -> {
            try {
                dispatchService.assignDriverToOrder(order1Id);
            } catch (Exception e) {
                log.error("Task 1 failed: {}", e.getClass().getName());
            } finally {
                latch.countDown();
            }
        };

        // ... create task2 ...

        executor.submit(task1);
        executor.submit(task2);
        latch.await(); // Wait for both to finish

        // 3. Assertions
        // Verify that Driver is BUSY
        // Verify only 1 Order is ASSIGNED, the other might be UNASSIGNED or failed.
    }
}

```

---

### **Task 4.2: The "Next Best Driver" Strategy (The Fix)**

We need to handle the collision. If the "Best" driver is taken by another thread while we were calculating, we shouldn't
fail—we should grab the "Second Best" driver.

**Requirements:**

1. Refactor `assignDriverToOrder` in `DispatchServiceImpl`.
2. Instead of a simple loop, implement a **Retry/Fallback Mechanism**.
3. **Logic:**

* Sort *all* reachable drivers by distance (nearest to farthest).
* Iterate through the sorted list.
* Try to `save(driver)` inside a `try-catch(ObjectOptimisticLockingFailureException)`.
* If it succeeds: Break and return.
* If it fails (someone else took him): **Log it and continue to the next driver in the list.**

**Tech Lead Tip:**

> You will need to manage the Transaction boundaries carefully. If a method is `@Transactional` and an exception is
> thrown, the *entire* transaction is marked for rollback. You might need to perform the "Driver locking" in a separate
> helper method or handle the exception programmatically.

**Skeleton:**

```java
// Logic flow hint
List<DriverAttempt> sortedDrivers = ...; // Calculate all paths first

        for(
DriverAttempt attempt :sortedDrivers){
        try{

// Try to lock this specific driver
assignDriver(order, attempt.driver());
        return...; // Success!
        }catch(
ObjectOptimisticLockingFailureException e){
        // Someone stole this driver. Continue loop to try the next best one.
        continue;
        }
        }

```

---

### **Task 4.3: Decoupling with Spring Events**

Right now, your API blocks while the pathfinder runs. If the graph is big, the user waits 2-3 seconds. Let's make it
asynchronous.

**Requirements:**

1. **Define Event:** Create `OrderCreatedEvent` (holds the `orderId`).
2. **Publish:** In `OrderService` (or Controller), when an order is created, publish this event using
   `ApplicationEventPublisher`.
3. **Listen:** Create `DispatchEventListener`.

* Annotate a method with `@EventListener` and `@Async`.
* Call `dispatchService.assignDriverToOrder(event.orderId())`.


4. **Enable Async:** Add `@EnableAsync` to your main application class.

**Why this matters:**
The user gets an immediate "HTTP 201 Created" response. The heavy lifting (graph calculation) happens in the background.

**Skeleton:**

```java
public record OrderCreatedEvent(UUID orderId) {
}

@Component
public class DispatchEventListener {

    @EventListener
    @Async // Run in a separate thread
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Call the service
    }
}

```

---

### **Acceptance Criteria for Day 4**

1. **Resilience:** If 2 orders fight for the same driver, the system automatically assigns the *second* order to the
   *next* available driver without throwing an error.
2. **Performance:** The `POST /orders` endpoint returns immediately, not waiting for the pathfinding algorithm.
3. **Test Coverage:** The `DispatchConcurrencyTest` passes with both orders being assigned (assuming 2 drivers exist, or
   1 succeeds and 1 waits gracefully).