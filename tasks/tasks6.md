## 1. WebSockets with STOMP

Long polling is resource-intensive because it repeatedly opens and closes connections. WebSockets provide a persistent,
bi-directional channel.

* **Task 1.1: Configuration:** Create a `WebSocketConfig` class implementing `WebSocketMessageBrokerConfigurer`.
  Register a STOMP endpoint (e.g., `/ws-registry`) and configure a simple Message Broker for a destination prefix like
  `/topic`.
* **Task 1.2: Server-to-Client Push:** In your `DispatchService` (or wherever the driver assignment logic lives), inject
  `SimpMessagingTemplate`. Once a driver is successfully assigned, call `convertAndSend()` to push the updated
  `OrderDTO` to a specific topic, such as `/topic/orders`.
* **Task 1.3: Frontend Update:** Update your HTML dashboard to use `Stomp.js` and `SockJS`. Replace the interval-based
  fetching with a subscription to the `/topic/orders` destination.

---

## 2. Security with Spring Security & JWT

You’ll be moving the application from "open" to "secured," which requires intercepting requests to validate tokens.

* **Task 2.1: Dependencies & JWT Utility:** Add `spring-boot-starter-security` and a JWT library (like `jjwt`). Create a
  `JwtUtils` class to handle token generation, parsing, and validation.
* **Task 2.2: The Filter:** Implement a `OncePerRequestFilter`. This filter will:

1. Extract the `Authorization` header.
2. Validate the JWT.
3. Set the `UsernamePasswordAuthenticationToken` in the `SecurityContextHolder` if the token is valid.


* **Task 2.3: Security Configuration:** Create a `SecurityConfig` (using the `@Bean SecurityFilterChain` approach).
* Permit access to the login/auth endpoints.
* Secure the `/api/orders/**` and `/dashboard` routes.
* Disable CSRF (standard for stateless JWT APIs) and set the Session Management to `SessionCreationPolicy.STATELESS`.

---

## 3. Testing: Mockito for DispatchService

Since the `DispatchService` handles race conditions and retries, we need to ensure the logic works even if the
database (repository) fails initially.

* **Task 3.1: Setup:** Create `DispatchServiceTest`. Use `@Mock` for your `OrderRepository` and `DriverRepository`, and
  `@InjectMocks` for the `DispatchService`.
* **Task 3.2: Mocking the Failure:** Use Mockito’s `when(...).thenThrow(...)` to simulate a `RuntimeException` or a
  `PessimisticLockingFailureException` on the first call to the repository.
* **Task 3.3: Verifying the Retry:** Use `verify(repository, times(2)).save(...)` to assert that the service actually
  attempted the operation again after the first failure. Assert that the final state of the order is "ASSIGNED."

---

### Success Criteria for Day 6

1. **Instant Dashboard:** When you create an order via Postman, the dashboard should update immediately without a page
   refresh or a network poll.
2. **403 Forbidden:** Attempting to fetch orders without a valid `Bearer <token>` should return a `403` or `401` error.
3. **Green Tests:** Your Mockito test should pass, proving the retry mechanism is robust against transient database
   issues.

**Would you like me to provide the specific `WebSocketConfig` boilerplate or the `JwtRequestFilter` implementation to
get you started?**

### **Prompt for asking about next day tasks in current chat**

So when I complete day 6 I can paste my code in the new chat?
Also tell me prompt so that I can get tasks for day 7

### **Prompt for Day 7**

I have successfully completed Day 6 of the GeoLogistics Orchestrator project. WebSockets are pushing real-time updates,
the API is secured with JWT, and I've verified my retry logic with Mockito unit tests.

Please provide the tasks for Day 7: Observability, Documentation, and Final Optimization. I want to focus on monitoring
the health of my services, documenting the API for other developers, and any final performance tuning.