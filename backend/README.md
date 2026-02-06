# Backend - Spring Boot REST API

Spring Boot 3.5.9 REST API with PostgreSQL and Caffeine in-memory caching.

## 🏗️ Architecture

```
src/main/java/net/profitwarning/api/
├── controller/       # REST endpoints (@RestController)
├── service/          # Business logic (@Service)
├── repository/       # Data access (Spring Data JPA)
├── model/            # JPA entities (@Entity)
├── dto/              # Data transfer objects
└── config/           # Configuration classes (@Configuration)
```

## 🚀 Running Locally

### From Root Directory (Recommended)

```bash
# Start backend only
npm run backend

# Start both frontend and backend
npm run dev
```

### From Backend Directory

```bash
cd backend
mvn spring-boot:run
```

### With Specific Profile

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

### Access Points

- **API Base URL:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/v3/api-docs
- **Health Check:** http://localhost:8080/actuator/health

## 🧪 Testing

### Run All Tests

```bash
# From root
npm run test:backend

# From backend directory
cd backend
mvn test
```

### Run Specific Test Class

```bash
cd backend
mvn test -Dtest=MessageControllerTest
```

### Run Specific Test Method

```bash
cd backend
mvn test -Dtest=MessageControllerTest#testCreateMessage
```

### Test with Coverage

```bash
cd backend
mvn clean test jacoco:report
```

Coverage report: `target/site/jacoco/index.html`

### Integration Tests

```bash
cd backend
mvn verify
```

## 📦 Building

### Development Build

```bash
# From root
npm run build:backend

# From backend directory
cd backend
mvn clean package
```

Output: `target/api-service-0.0.1-SNAPSHOT.jar`

### Production Build (Skip Tests)

```bash
cd backend
mvn clean package -DskipTests
```

### Build Docker Image

```bash
cd backend
docker build -t api-service:latest .
```

### Run Docker Container

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/api_db \
  api-service:latest
```

## 🗄️ Database

### Flyway Migrations

Migration files location: `src/main/resources/db/migration/`

#### Available Migrations

| File | Description |
|------|-------------|
| `V1__Create_testmessages_table.sql` | Initial schema with testmessages table |
| `V2__Add_created_at_to_testmessages.sql` | Add created_at timestamp column |

#### Run Migrations

```bash
# From root
npm run db:migrate

# From backend directory
cd backend
mvn flyway:migrate
```

#### Migration Info

```bash
# From root
npm run db:info

# From backend directory
cd backend
mvn flyway:info
```

#### Clean Database (⚠️ Destructive)

```bash
# From root
npm run db:clean

# From backend directory
cd backend
mvn flyway:clean
```

### Database Schema

#### testmessages Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Auto-incrementing ID |
| `message` | VARCHAR(255) | NOT NULL | Message content |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |

**Note:** Java field is named `content`, mapped to DB column `message` via `@Column(name = "message")`

## ⚙️ Configuration

### Application Profiles

| Profile | Description | Use Case |
|---------|-------------|----------|
| `dev` | Development profile | Local development with PostgreSQL |
| `docker` | Docker Compose environment | Running in devcontainer |

### Profile Configuration Files

- `application.properties` - Common configuration
- `application-dev.properties` - Development overrides
- `application-docker.properties` - Docker environment

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_PROFILES_ACTIVE` | Active profile | `dev` | No |
| `DATABASE_URL` | PostgreSQL JDBC URL | `localhost:5432/api_db` | Yes |
| `DATABASE_USERNAME` | Database username | `postgres` | Yes |
| `DATABASE_PASSWORD` | Database password | `postgres` | Yes |
| `CACHE_MAX_SIZE` | Maximum cache entries | `1000` | No |
| `SERVER_PORT` | Application port | `8080` | No |

### CORS Configuration

Configured in `src/main/java/net/profitwarning/api/config/WebConfig.java`

**Allowed Origins:**
- `http://localhost:4200`
- `http://0.0.0.0:4200`

**Allowed Methods:** GET, POST, PUT, DELETE, OPTIONS

**Allowed Headers:** All (`*`)

## 📡 API Documentation

### Swagger UI

Interactive API documentation: http://localhost:8080/swagger-ui.html

### OpenAPI Specification

JSON format: http://localhost:8080/v3/api-docs

### API Endpoints

#### Messages API (`/api/v1/messages`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/v1/messages` | Get all messages | - | `MessageResponse[]` |
| GET | `/api/v1/messages/{id}` | Get message by ID | - | `MessageResponse` |
| POST | `/api/v1/messages` | Create new message | `CreateMessageCommand` | `MessageResponse` |

#### Request/Response Examples

**Create Message:**
```bash
curl -X POST http://localhost:8080/api/v1/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "Hello World"}'
```

**Response:**
```json
{
  "id": 1,
  "content": "Hello World",
  "createdAt": "2026-01-15T10:30:00Z"
}
```

**Get All Messages:**
```bash
curl http://localhost:8080/api/v1/messages
```

**Get Message by ID:**
```bash
curl http://localhost:8080/api/v1/messages/1
```

## 🔧 Dependencies

### Core Dependencies

| Dependency | Purpose | Version |
|------------|---------|---------|
| `spring-boot-starter-web` | REST API support | 3.5.9 |
| `spring-boot-starter-data-jpa` | Database access | 3.5.9 |
| `spring-boot-starter-cache` | Caching abstraction | 3.5.9 |
| `caffeine` | High-performance in-memory cache | Managed by Spring Boot |

### Database

| Dependency | Purpose |
|------------|---------|
| `postgresql` | PostgreSQL JDBC driver |
| `flyway-core` | Database migrations |
| `flyway-database-postgresql` | Flyway PostgreSQL support |

### Development

| Dependency | Purpose |
|------------|---------|
| `spring-boot-devtools` | Hot reload support |
| `spring-boot-actuator` | Production-ready features |

### Testing

| Dependency | Purpose |
|------------|---------|
| `spring-boot-starter-test` | Testing framework |
| `testcontainers` | Container-based testing |
| `testcontainers-postgresql` | PostgreSQL test containers |
| `mockito-core` | Mocking framework |

### Documentation

| Dependency | Purpose |
|------------|---------|
| `springdoc-openapi-starter-webmvc-ui` | OpenAPI/Swagger |

## 🔄 Caching Strategy

### Caffeine In-Memory Cache

- **Cache Manager:** `CaffeineCacheManager` (Spring Boot auto-configured)
- **Cache Provider:** Caffeine - High-performance Java 8+ caching library
- **Storage:** In-memory (JVM heap)
- **Persistence:** None - cache is cleared on application restart
- **TTL:** 10 minutes (600 seconds)
- **Max Entries:** Configurable via `CACHE_MAX_SIZE` (default: 1000)
- **Eviction Policy:** Size-based LRU (Least Recently Used)

### Configuration

**Cache Size Configuration:**
```bash
# Via environment variable
export CACHE_MAX_SIZE=5000

# Via application properties
spring.cache.caffeine.spec=maximumSize=5000,expireAfterWrite=10m
```

**Cache Specification:**
- `maximumSize` - Maximum number of entries before eviction (default: 1000)
- `expireAfterWrite` - TTL after entry is written (10 minutes)

### Cached Operations

| Cache Name | Method | Key | TTL | Max Size | Eviction Trigger |
|------------|--------|-----|-----|----------|------------------|
| `testMessages` | `getMessages()` | `SimpleKey []` | 10 min | 1000 | On `saveMessage()` |
| `testMessages` | `getMessageById(Long id)` | `#id` | 10 min | 1000 | On `saveMessage()` |

### Cache Annotations

**Service Implementation:** `TestMessageService.java`

```java
// Cache all messages (list)
@Cacheable(value = "testMessages")
public List<TestMessage> getMessages() { ... }

// Cache individual message by ID
@Cacheable(value = "testMessages", key = "#id")
public Optional<TestMessage> getMessageById(Long id) { ... }

// Evict entire cache on write operations
@CacheEvict(value = "testMessages", allEntries = true)
public TestMessage saveMessage(TestMessage message) { ... }
```

### Cache Behavior

1. **First Request (Cache Miss):**
   - Data fetched from PostgreSQL
   - Result stored in Caffeine cache
   - Subsequent requests served from memory

2. **Subsequent Requests (Cache Hit):**
   - Data retrieved directly from in-memory cache
   - No database query (ultra-fast response)

3. **Cache Eviction:**
   - **On Write:** Entire cache cleared when `saveMessage()` is called
   - **On TTL:** Entries automatically expire after 10 minutes
   - **On Size:** LRU eviction when cache exceeds max size

4. **Application Restart:**
   - All cache entries lost (in-memory only)
   - Cache rebuilt on subsequent requests

### Performance Characteristics

| Aspect | Caffeine | Redis (Previous) |
|--------|----------|------------------|
| **Latency** | ~50-100 nanoseconds | ~1-5 milliseconds |
| **Throughput** | Millions ops/sec | 100K+ ops/sec |
| **Network** | None (in-process) | Network overhead |
| **Persistence** | None | Optional |
| **Shared Cache** | No (per-instance) | Yes (multi-instance) |

### Multi-Instance Considerations

⚠️ **Important:** Caffeine is an in-memory cache local to each application instance. If you run multiple instances:

- Each instance has its own separate cache
- Cache updates are NOT shared across instances
- Cache inconsistency may occur between instances
- For multi-instance deployments with shared cache needs, consider Redis or other distributed caching solutions

### Monitoring Cache Performance

```java
// Access cache statistics (if enabled)
CacheManager cacheManager;
Cache cache = cacheManager.getCache("testMessages");
// Use cache.getNativeCache() for Caffeine-specific stats
```

## 🐛 Known Issues

### 1. Lombok Compilation Issue

**Problem:** Lombok's `@Data`, `@Getter`, `@Setter` annotations caused circular dependency during Maven compilation.

**Solution:** Removed Lombok annotations from `TestMessage.java` and added manual getters/setters.

**File:** `src/main/java/net/profitwarning/api/model/TestMessage.java`

### 2. Field Name Mapping

**Issue:** Java field name differs from database column name.

- **Java Field:** `content`
- **Database Column:** `message`

**Solution:** Using `@Column(name = "message")` annotation for mapping.

```java
@Column(name = "message", nullable = false)
private String content;
```

## 📊 Code Structure

### Layers

```
Controller Layer (REST endpoints)
    ↓
Service Layer (Business logic)
    ↓
Repository Layer (Data access)
    ↓
Database (PostgreSQL)
```

### Package Organization

- `controller/` - REST controllers, request/response handling
- `service/` - Business logic, transactions, caching
- `repository/` - Spring Data JPA repositories
- `model/` - JPA entities, database models
- `dto/` - Data transfer objects (requests/responses)
- `config/` - Configuration classes (CORS, cache, etc.)

### Naming Conventions

- **Entities:** Singular nouns (e.g., `TestMessage`)
- **Repositories:** `EntityNameRepository` (e.g., `TestMessageRepository`)
- **Services:** `EntityNameService` (e.g., `TestMessageService`)
- **Controllers:** `EntityNameController` (e.g., `MessageController`)
- **DTOs:** Purpose-based (e.g., `CreateMessageCommand`, `MessageResponse`)

## 🧹 Code Quality

### Maven Commands

```bash
# Compile code
mvn compile

# Run tests
mvn test

# Check code coverage
mvn test jacoco:report

# Package application
mvn package

# Clean build artifacts
mvn clean
```

### Recommended Plugins

- **Checkstyle:** Code style enforcement
- **SpotBugs:** Static analysis
- **JaCoCo:** Code coverage
- **SonarQube:** Code quality analysis

## 🚀 Performance Tips

1. **Database Indexing:** Ensure proper indexes on frequently queried columns
2. **Caffeine Caching:** Leverage in-memory caching for read-heavy operations
3. **Connection Pooling:** Configure HikariCP for optimal database connections
4. **Lazy Loading:** Use `@Lazy` for beans that aren't always needed
5. **Async Processing:** Consider `@Async` for long-running operations

## 📝 Development Workflow

1. **Create Feature Branch:**
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Make Changes and Test:**
   ```bash
   npm run test:backend
   ```

3. **Build Application:**
   ```bash
   npm run build:backend
   ```

4. **Run Locally:**
   ```bash
   npm run backend
   ```

5. **Commit and Push:**
   ```bash
   git add .
   git commit -m "feat(backend): add my feature"
   git push origin feature/my-feature
   ```

## 🐳 Docker

### Production Dockerfile

The backend includes a production-ready Dockerfile with:
- Multi-stage build (Maven builder + JRE runtime)
- Non-root user (`spring:spring`)
- Optimized for size (~200 MB)
- Based on Eclipse Temurin JRE 25 Alpine

### Building the Image

```bash
# From project root (recommended)
npm run docker:build:backend

# Or from backend directory
cd backend
docker build -t spring-angular-template/backend:latest .

# Tag for registry
docker tag spring-angular-template/backend:latest myregistry.com/backend:1.0.0
```

### Running Standalone

```bash
# Run backend container (requires database)
docker run -d \
  --name spring-backend \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/apidb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  spring-angular-template/backend:latest

# View logs
docker logs -f spring-backend

# Access health check
curl http://localhost:8080/actuator/health
```

### Production Stack

For complete production environment with database and frontend:

```bash
# From project root
npm run docker:prod

# Access API
curl http://localhost:8080/api/v1/messages
```

### Environment Variables

All Spring Boot properties can be configured via environment variables:

```bash
docker run -d \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/apidb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=secret \
  -e CACHE_MAX_SIZE=5000 \
  spring-angular-template/backend:latest
```

Notes:
- `CACHE_MAX_SIZE` applies to `java-25-caffeine-angular-21`.

### Build Optimization

The Dockerfile uses `.dockerignore` to exclude:
- Build artifacts (`target/`)
- IDE files (`.idea/`, `.vscode/`)
- Documentation files
- Git repository

This reduces build context and speeds up image builds.

### Health Checks

The application exposes a health endpoint via Spring Boot Actuator:
- **Health:** `GET /actuator/health`

This is used by `docker-compose.prod.yml` for container health monitoring.

### Further Documentation

See [DOCKER.md](../DOCKER.md) for comprehensive Docker documentation including:
- Production deployment strategies
- Security hardening
- CI/CD integration
- Troubleshooting

## 🔗 Related Documentation

- **[Root README](../README.md)** - Monorepo overview
- **[Frontend README](../frontend/README.md)** - Angular application

---

**Backend Status:** Active Development  
**Spring Boot Version:** 3.5.9  
**Java Version:** 25  
**Last Updated:** January 30, 2026
