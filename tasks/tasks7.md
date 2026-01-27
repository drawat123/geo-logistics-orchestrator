### 📋 Day 7: Observability, Docs & Optimization

#### **Task 1: Implement Observability (Spring Boot Actuator)**

You cannot fix what you cannot see. Actuator adds "production-ready" features to your app, allowing you to monitor
health, metrics, and traffic.

* **Sub-task 1.1: Add Dependencies**
* Add `spring-boot-starter-actuator`.


* **Sub-task 1.2: Expose Endpoints**
* Enable `health`, `info`, `metrics`, and `loggers` in `application.properties`.
* *Why:* This lets you check if the DB is down (`/health`) or how much memory is used (`/metrics`).


* **Sub-task 1.3: Secure the Endpoints**
* **Crucial:** You do not want the public to see your server metrics.
* Update `SecurityConfig` to ensure only `ROLE_ADMIN` can access `/actuator/**`.

#### **Task 2: Automated API Documentation (Swagger/OpenAPI)**

Stop writing Postman collections manually. We will auto-generate interactive documentation that stays in sync with your
code.

* **Sub-task 2.1: Add `springdoc-openapi**`
* Dependency: `springdoc-openapi-starter-webmvc-ui`.


* **Sub-task 2.2: Configure JWT in Swagger**
* By default, Swagger doesn't know you use JWTs. You need a `@Configuration` bean to add the "Authorize" padlock button
  to the UI.
* This allows you to paste your token once and test all endpoints directly in the browser.


* **Sub-task 2.3: Annotate Controllers**
* Use `@Operation(summary = "...")` to describe what your endpoints do.
* Use `@ApiResponse(responseCode = "403")` to document error states.

#### **Task 3: Database Performance (Indexing)**

Your queries are fast now because you have 10 rows. When you have 10,000, they will freeze without indexes.

* **Sub-task 3.1: Index Key Columns**
* Add `@Index` to the `User` table for `email` (used in Login).
* Add `@Index` to the `Order` table for `driver_id` and `status` (used heavily in the Dispatcher).


* **Sub-task 3.2: Verify Query Plans**
* (Optional) Enable `spring.jpa.show-sql=true` and visually check that you aren't doing "N+1" queries when fetching
  orders.

---

### 🛠️ Code Snippets for Day 7

#### 1. `pom.xml` Updates

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<dependency>
<groupId>org.springdoc</groupId>
<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
<version>2.3.0</version>
</dependency>

```

#### 2. Swagger Configuration (With JWT Support)

Create `config/OpenApiConfig.java`. This enables the "Authorize" button.

```java

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("GeoLogistics API").version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}

```

#### 3. Security Config Update

You must allow access to the Swagger UI HTML pages while locking down Actuator.

```java
// Inside SecurityFilterChain ...
.authorizeHttpRequests(auth ->auth
        .

requestMatchers("/auth/**").

permitAll()
// ALLOW SWAGGER (Public Docs)
    .

requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").

permitAll()
// LOCK ACTUATOR (Admins Only)
    .

requestMatchers("/actuator/**").

hasRole("ADMIN") 
    .

anyRequest().

authenticated()
)

```

#### 4. Entity Indexing Example

Update your `Order` entity to speed up lookups.

```java

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_driver", columnList = "driver_id"),
        @Index(name = "idx_order_status", columnList = "status")
})
public class Order { ...
}

```

---

### 🚀 Recommended Execution Order

1. **Start with Swagger:** It gives you an immediate visual reward (a UI website for your API) and makes testing the
   rest easier.
2. **Add Actuator:** Secure it and verify you can see the health JSON.
3. **Optimize DB:** Add the indexes and restart to let Hibernate generate the DDL.

### **Prompt for asking about next day tasks in current chat**

Tell me prompt so that I can get tasks for day 8

### **Prompt for Day 8**

> I have completed the core backend for my 'GeoLogistics Orchestrator' using Spring Boot, PostgreSQL, and WebSockets.
> **Goal for Day 8:**
> I want to optimize the **Real-Time Location Tracking** component by introducing **Redis**. Currently, I am querying
> PostgreSQL for driver locations, which is not scalable for high-frequency updates (like Uber).
> **Please provide a detailed task list and code guide for:**
> 1. **Infrastructure Setup:**
> * Adding `spring-boot-starter-data-redis`.
> * Updating `docker-compose.yml` to include a Redis container.
> * Configuring `RedisTemplate` in Spring Boot for JSON serialization.
>
>
> 2. **Redis Geospatial Logic:**
> * Creating a `LocationService`.
> * Using **`GEOADD`** to update a driver's live location every 5 seconds.
> * Using **`GEORADIUS`** (or `GEUSEARCH`) to find the 5 nearest drivers to a pickup point.
>
>
> 3. **Refactoring Dispatch Logic:**
> * Updating my `DispatchService` to query Redis for available drivers first, then fetch their full profile from
    PostgreSQL (Hybrid approach).
> * Handling **TTL (Time To Live)** so that stale driver locations automatically expire if they stop sending updates.
>
>
> 4. **Testing:**
> * A simple Controller endpoint to simulate a driver moving (updating Redis) and an Admin endpoint to query 'Who is
    near me?'