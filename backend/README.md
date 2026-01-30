# Backend - Spring Boot REST API

Spring Boot 3.5.9 REST API with PostgreSQL and Redis caching.

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
| `openshift` | Red Hat OpenShift | Production deployment |

### Profile Configuration Files

- `application.properties` - Common configuration
- `application-dev.properties` - Development overrides
- `application-docker.properties` - Docker environment
- `application-openshift.properties` - OpenShift deployment

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_PROFILES_ACTIVE` | Active profile | `dev` | No |
| `DATABASE_URL` | PostgreSQL JDBC URL | `localhost:5432/api_db` | Yes |
| `DATABASE_USERNAME` | Database username | `postgres` | Yes |
| `DATABASE_PASSWORD` | Database password | `postgres` | Yes |
| `REDIS_HOST` | Redis hostname | `localhost` | Yes |
| `REDIS_PORT` | Redis port | `6379` | No |
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
| `spring-data-redis` | Redis integration | - |

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

### Redis Configuration

- **Cache Manager:** `RedisCacheManager`
- **Serialization:** JSON
- **TTL:** Configurable per cache

### Cached Operations

| Cache Name | Method | TTL | Eviction |
|------------|--------|-----|----------|
| `messages` | `getAllMessages()` | 5 minutes | On create/update/delete |
| `message` | `getMessageById()` | 5 minutes | On update/delete |

### Cache Annotations

```java
@Cacheable(value = "messages")
public List<MessageResponse> getAllMessages() { ... }

@CacheEvict(value = "messages", allEntries = true)
public MessageResponse createMessage(...) { ... }
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
2. **Redis Caching:** Leverage caching for read-heavy operations
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

## 🔗 Related Documentation

- **[Root README](../README.md)** - Monorepo overview
- **[Frontend README](../frontend/README.md)** - Angular application
- **[OpenShift Deployment](../docs/planning/OPENSHIFT_DEPLOYMENT.md)** - Production deployment

---

**Backend Status:** Active Development  
**Spring Boot Version:** 3.5.9  
**Java Version:** 25  
**Last Updated:** January 30, 2026
