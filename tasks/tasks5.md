### **Part 1: Optimization (Caching in Graph Service)**

Calculating the "Nearest Node" (Geo-spatial calculation) is CPU intensive. If a user requests a dispatch from the same
location (e.g., a popular mall or office), we shouldn't re-compute the geometry every time.

**1. Add Dependencies**
In your `pom.xml`, add the Spring Boot Cache starter.

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
<groupId>com.github.ben-manes.caffeine</groupId>
<artifactId>caffeine</artifactId>
</dependency>

```

**2. Enable Caching**
Add `@EnableCaching` to your main application class or a `CacheConfig` class.

**3. Annotate the Service Method**
Modify your `GraphService` (or `GeoService`).

* **@Cacheable:** Skips the method execution if the key exists in the cache.
* **Key Generation:** We need a unique key based on coordinates.

```java

@Service
public class GraphService {

    @Cacheable(value = "nearestNode", key = "#lat + '-' + #lon")
    public Node findNearestNode(double lat, double lon) {
        // ... expensive geometric calculation ...
        simulateDelay(); // To prove cache works later
        return node;
    }

    private void simulateDelay() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }
}

```

**Verification:**

* Call the API with coordinates `(12.97, 77.59)`. It should take ~2 seconds (due to simulated delay).
* Call it again immediately. It should return instantly (ms) because it hit the cache.

---

### **Part 2: Visualization (Simple Order Dashboard)**

We need a way to see the status transition (`PENDING` -> `ASSIGNED`) without staring at logs. A simple HTML page with *
*Long Polling** is sufficient for this stage.

**1. Enable CORS**
Since your HTML file will likely run on a different port (or just from the file system), allow Cross-Origin requests in
your Controller.

```java

@CrossOrigin(origins = "*") // For development only
@GetMapping("/{orderId}")
public OrderDTO getOrder(@PathVariable String orderId) { ...}

```

**2. The Frontend (dashboard.html)**
Create a simple HTML file. No React/Angular needed yet; raw JS is fine for debugging.

```html
<!DOCTYPE html>
<html>
<head>
    <title>Dispatch Dashboard</title>
    <style>
        .status { padding: 10px; border-radius: 5px; font-weight: bold; }
        .PENDING { background-color: #f39c12; }
        .ASSIGNED { background-color: #2ecc71; }
    </style>
</head>
<body>
<h2>Order Monitor</h2>
<input type="text" id="orderIdInput" placeholder="Enter Order ID">
<button onclick="startTracking()">Track</button>

<div id="result" style="margin-top: 20px;">
    Status: <span id="statusBadge" class="status">WAITING</span>
</div>

<script>
    let intervalId;

    function startTracking() {
        const orderId = document.getElementById('orderIdInput').value;
        if (intervalId) clearInterval(intervalId);
        
        // Poll every 2 seconds
        intervalId = setInterval(() => checkStatus(orderId), 2000);
    }

    async function checkStatus(id) {
        try {
            // Adjust port/endpoint as per your implementation
            const response = await fetch(`http://localhost:8080/orders/${id}`);
            const data = await response.json();
            
            const badge = document.getElementById('statusBadge');
            badge.innerText = data.status;
            badge.className = "status " + data.status;

            // Stop polling if final state reached
            if (data.status === 'ASSIGNED') {
                clearInterval(intervalId);
                alert("Order Assigned to Driver: " + data.driverId);
            }
        } catch (e) {
            console.error("Polling error", e);
        }
    }
</script>
</body>
</html>

```

---

### **Part 3: Final Polish (The "Professional" Touch)**

To make this project portfolio-ready (and FAANG-ready), the documentation and build process must be clean.

**1. Dockerfile**
Create a `Dockerfile` in the root directory to containerize the app.

```dockerfile
# Build Stage
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Run Stage
FROM openjdk:17-jdk-slim
COPY --from=build /target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

```

**2. README.md Checklist**
A strong README distinguishes a hobby project from a professional one. Ensure you have:

* **Architecture Diagram:** A simple MermaidJS diagram showing Controller -> Service -> Event -> Listener.
* **Concurrency Strategy:** Explicitly explain *how* you solved the race condition (e.g., "Used `Pessimistic Locking` on
  the Driver table...").
* **Tech Stack:** Java 17, Spring Boot 3, H2/PostgreSQL, Spring Events.
* **How to Run:** `docker build ...` and `docker run ...` commands.

### **Summary of Day 5 Deliverables**

1. **Graph Service** caches "Nearest Node" results.
2. **Dashboard.html** visually flips from Orange (Pending) to Green (Assigned).
3. **Docker** container allows the app to run anywhere.
4. **Use logger** instead of sout calls