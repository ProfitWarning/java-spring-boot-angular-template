# Spring Boot Development Guidelines

This document provides coding standards and best practices for Spring Boot development in this project.

## Technology Stack

- **Spring Boot:** 3.5.9
- **Java:** 25
- **PostgreSQL:** 17
- **Maven:** 3.9.6

## Core Principles

### 1. Dependency Injection

✅ **Prefer Constructor Injection**
- Declare all mandatory dependencies as `final` fields
- Inject them through the constructor
- Spring auto-detects single constructors (no `@Autowired` needed)
- ❌ Avoid field/setter injection in production code

```java
class OrderService {
    private final OrderRepository repository;
    private final EmailService emailService;
    
    // Constructor injection (no @Autowired needed)
    OrderService(OrderRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }
}
```

### 2. Visibility Modifiers

✅ **Prefer package-private over public**
- Controllers and their methods: package-private when possible
- `@Configuration` classes: package-private
- `@Bean` methods: package-private
- No obligation to make everything `public`

### 3. Configuration Management

✅ **Use Typed Properties**
- Group related properties with a common prefix
- Bind to `@ConfigurationProperties` classes
- Add validation annotations for fail-fast behavior
- ✅ Prefer environment variables over Spring profiles

```java
@ConfigurationProperties(prefix = "app.cache")
@Validated
record CacheProperties(
    @Min(100) int maxSize,
    @NotNull Duration ttl
) {}
```

### 4. Transaction Management

✅ **Define Clear Boundaries**
- Each service method = one transactional unit
- Query methods: `@Transactional(readOnly = true)`
- Data-modifying methods: `@Transactional`
- Keep transaction scope minimal

```java
@Transactional(readOnly = true)
public List<Order> findAllOrders() {
    return repository.findAll();
}

@Transactional
public Order createOrder(CreateOrderCommand command) {
    // Transaction scope
}
```

### 5. JPA Configuration

❌ **Disable Open Session in View**

Add to `application.properties`:
```properties
spring.jpa.open-in-view=false
```

### 6. Layer Separation

✅ **Separate Web from Persistence**
- ❌ Never expose entities directly in controllers
- ✅ Define explicit request/response DTOs (use records)
- ✅ Apply Jakarta Validation on request DTOs

```java
record CreateOrderRequest(
    @NotBlank String customerName,
    @Min(1) int quantity
) {}

record OrderResponse(
    Long id,
    String customerName,
    OrderStatus status
) {}
```

### 7. REST API Design

✅ **Follow REST Principles**

- **Versioned URLs:** `/api/v1/resources`
- **Resource-oriented:** `/api/v1/orders`, `/api/v1/orders/{id}/items`
- **Consistent patterns:** Uniform URL conventions
- **Explicit status codes:** Use `ResponseEntity<T>`
- **Pagination:** For unbounded collections
- **JSON structure:** Use objects as top-level (not arrays)
- **Naming:** Consistent snake_case or camelCase

```java
@GetMapping("/api/v1/orders/{id}")
ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
    return orderService.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}
```

### 8. Command Pattern

✅ **Use Command Objects**
- Create purpose-built command records
- Wrap input data for business operations
- Pass to service methods

```java
record CreateOrderCommand(String customerName, int quantity) {}

@Transactional
public Order createOrder(CreateOrderCommand command) {
    // Business logic
}
```

### 9. Exception Handling

✅ **Centralize Error Handling**
- Use `@RestControllerAdvice` for REST APIs
- Define `@ExceptionHandler` methods
- Return consistent error responses
- Consider RFC 9457 ProblemDetails format

```java
@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail handleOrderNotFound(OrderNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, 
            ex.getMessage()
        );
    }
}
```

### 10. Actuator Endpoints

✅ **Secure Appropriately**
- Expose `/health`, `/info`, `/metrics` without auth
- ✅ Secure all other actuator endpoints

### 11. Internationalization

✅ **Externalize User-Facing Text**
- Use ResourceBundles for labels, prompts, messages
- ❌ Never hardcode user-facing strings in code

### 12. Testing

✅ **Use Testcontainers**
- Spin up real services in integration tests
- Mirror production environments

✅ **Random Ports**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {
    // Tests
}
```

### 13. Logging

✅ **Proper Logging Framework**
- ✅ Use SLF4J with Logback/Log4j2
- ❌ Never use `System.out.println()`

✅ **Protect Sensitive Data**
- ❌ Never log credentials, PII, or confidential data

✅ **Guard Expensive Calls**
```java
// Level check
if (logger.isDebugEnabled()) {
    logger.debug("State: {}", computeExpensiveDetails());
}

// Lambda/Supplier approach
logger.atDebug()
    .setMessage("State: {}")
    .addArgument(() -> computeExpensiveDetails())
    .log();
```

## Package Structure

```
net.profitwarning.api/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access
├── model/          # JPA entities
├── dto/            # Request/Response objects
├── config/         # Configuration classes
└── exception/      # Custom exceptions
```

## Naming Conventions

- **Entities:** Singular nouns (e.g., `Order`, `Customer`)
- **Repositories:** `{Entity}Repository` (e.g., `OrderRepository`)
- **Services:** `{Entity}Service` (e.g., `OrderService`)
- **Controllers:** `{Entity}Controller` (e.g., `OrderController`)
- **DTOs:** Purpose-based (e.g., `CreateOrderRequest`, `OrderResponse`)

## Additional Resources

- [RFC 9457 - Problem Details](https://www.rfc-editor.org/rfc/rfc9457)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
