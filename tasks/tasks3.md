## **Day 3: The Dispatch Logic (Spatial Search)**

**Objective:**
Currently, your Graph (Nodes/Edges) is isolated from your Database (Drivers/Orders).
Real-world GPS coordinates rarely match a "Node" exactly. A driver might be at `(12.972, 77.591)`, but the nearest road
intersection (Node) is at `(12.974, 77.595)`.

Today, we will implement the **"Snap to Road"** logic (Nearest Neighbor Search) and build the **Dispatch Service** that
assigns the closest driver to an order.

---

### **Task 3.1: Nearest Neighbor Search (The "Snap" Function)**

We need to translate raw GPS coordinates (from a Driver or Order) into a valid `startNodeId` for our pathfinder.

**Requirements:**

1. Add a method `findNearestNode(double lat, double lon)` to your `CityGraphService`.
2. **Algorithm:** Since our graph is small (under 10k nodes), use a **Linear Search** (). Iterate through all nodes in
   the registry and find the one with the minimum Euclidean/Haversine distance to the input coordinates.
3. **Return:** The `LocationNode` (or its ID) that is closest.

**Interface Update:**

```java
public interface CityGraphService {
    // ... previous methods ...

    // New Method
    LocationNode findNearestNode(double lat, double lon);
}

```

> **DSA Note:** In a massive system (millions of nodes), is too slow. You would use a **QuadTree** or **k-d tree** ().
> For now, Linear Search is acceptable.

---

### **Task 3.2: The Dispatch Service (Business Logic)**

This is the brain of the application. It connects the **Persistence Layer** (Day 1) with the **Graph Layer** (Day 2).

**Requirements:**

1. Create `DispatchService`.
2. Inject `OrderRepository`, `DriverRepository`, `CityGraphService`, and `PathfinderService`.
3. Implement `assignDriverToOrder(UUID orderId)`.

**The Algorithm:**

1. **Fetch Order:** Get the order from the DB.
2. **Fetch Drivers:** Get all "IDLE" drivers from the DB.
3. **Snap Order:** Use `findNearestNode` to find the graph node closest to the Order. call this `TargetNode`.
4. **Evaluate Drivers:**

* For each driver, use `findNearestNode` to find their `StartNode`.
* Run `pathfinder.findShortestPath(StartNode, TargetNode)`.
* Keep track of the driver with the **lowest distance/cost**.


5. **Assign:** Update the selected Driver's status to "BUSY" and link them to the order (if your DB schema allows).
6. **Return:** A DTO containing the assigned `driverId`, the `distance`, and the `eta` (assume avg speed 40km/h).

**Skeleton:**

```java
public record DispatchResult(UUID driverId, double distanceKm, double estimatedTimeMinutes) {
}

public interface DispatchService {
    DispatchResult assignDriverToOrder(UUID orderId);
}

```

---

### **Task 3.3: The REST Controller (Integration)**

Expose this logic to the outside world.

**Requirements:**

1. Create `DispatchController`.
2. Endpoint: `POST /api/orders/{orderId}/dispatch`.
3. It should call the service and return the assignment details.
4. Handle `PathNotFoundException` gracefully (return HTTP 404 or 422).

---

### **Acceptance Criteria for Day 3**

1. You can insert an Order into Postgres (via SQL or a simple create endpoint).
2. You have seeded the Graph (from Day 2).
3. When you hit the Dispatch API:

* The system correctly identifies which seeded Graph Node is closest to the SQL Driver.
* It calculates the path.
* It returns the closest driver.

**Tech Lead Tip:**

> Don't worry about "Booking conflicts" (two orders grabbing the same driver) yet. We will handle Concurrency and
> Locking in **Day 4**. For now, assume single-threaded requests.

Ready to implement the **"Snap to Road"** logic?