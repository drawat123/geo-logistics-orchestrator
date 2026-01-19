## **Day 2: The Routing Engine (Graph Theory)**

**Objective:** We are building the core navigation system. In a real-world scenario, you might use OSRM or Google Maps
API, but for this learning project, you will build a weighted directed graph and a pathfinder from scratch to master the
DSA concepts.

### **Task 2.1: Define the Graph Elements**

We need to represent the city. A "Node" is a delivery point or intersection. An "Edge" is the road connecting them,
which has a "weight" (cost).

**Requirements:**

1. Create a `LocationNode` class. It needs a unique ID and coordinates.
2. Create a `RoadEdge` class. It must connect a source to a destination and have a `weight`.

* *Design decision:* For logistics, "weight" is rarely just distance. It is usually **Time** (Distance / Speed). For
  this exercise, assume `weight = distance` for simplicity, but design the class so we can easily swap this for "travel
  time" later.

**Skeleton:**

```java
public record LocationNode(String id, double lat, double lon) {
    // Helper to calculate Euclidean distance to another node
    public double distanceTo(LocationNode other) {
        // Implement Haversine or simple Euclidean distance
        return 0.0;
    }
}

public record RoadEdge(LocationNode target, double weight) {
    // Simple POJO/Record to hold the destination and cost
}

```

---

### **Task 2.2: The Adjacency List (In-Memory Graph)**

We need a service that holds the map in memory. We will use an **Adjacency List** because road networks are sparse
graphs (most intersections only connect to 3-4 others).

**Requirements:**

1. Create `CityGraphService`.
2. Use a `Map` to represent the adjacency list.
3. Implement methods to add nodes and add edges.
4. **Constraint:** The graph is **Directed**. (A B does not imply B A, e.g., one-way streets).

**Skeleton:**

```java
public interface CityGraphService {
    void addLocation(LocationNode node);

    // Connects sourceId -> targetId with a specific weight
    void addRoad(String sourceId, String targetId, double weight);

    // Returns the list of outgoing roads from a specific location
    List<RoadEdge> getAdjacencyList(String nodeId);

    // Check if a node exists
    boolean containsNode(String nodeId);
}

```

---

### **Task 2.3: Dijkstra’s Algorithm (The Core Logic)**

Now, implement the pathfinding.

**Requirements:**

1. Create a `PathfinderService`.
2. Implement `findShortestPath` using **Dijkstra’s Algorithm**.
3. **Data Structure:** You must use a `PriorityQueue` (Min-Heap) for the algorithm to be efficient ().
4. **Output:** Return a `PathResult` containing the total cost and the ordered list of `LocationNode`s visited.

**Skeleton:**

```java
public record PathResult(double totalDistance, List<LocationNode> path) {
}

public interface PathfinderService {
    /**
     * @param graph The city graph data source
     * @param startNodeId The UUID of the starting location (e.g., Driver location)
     * @param endNodeId The UUID of the delivery target
     * @return PathResult or throws PathNotFoundException
     */
    PathResult findShortestPath(CityGraphService graph, String startNodeId, String endNodeId);
}

```

**Tech Lead Tip:**

> Remember to track `visited` nodes to avoid cycles. Also, in your Priority Queue, you'll need a helper class (e.g.,
`NodeWrapper`) that implements `Comparable` so the queue knows which node has the smallest tentative distance.

---

### **Task 2.4: Integration & Seeding (The "Hello World" of Maps)**

We need to verify this works before connecting it to the database.

**Requirements:**

1. Create a `CommandLineRunner` or a `@PostConstruct` method in a generic configuration class.
2. Seed a small "Mock City":

* **Nodes:** A, B, C, D, E.
* **Edges:** A->B (10), A->C (5), C->B (2), B->D (1), C->D (8), D->E (2).


3. Print the shortest path from **A to E** to the console on startup.

**Expected Output for Seeding:**
Calculated Path: A C B D E
Total Cost: .

---

### **Acceptance Criteria for Day 2**

1. `CityGraphService` can store nodes and edges.
2. `PathfinderService` correctly identifies the shortest path on the mock data.
3. No external graph libraries (like JGraphT) are used.
4. Unit test created for the `findShortestPath` method covering:

* Standard path.
* No path exists (disconnected graph).
* Start node == End node (distance 0).