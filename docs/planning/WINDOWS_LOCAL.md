### Plan: Windows Local Development

This plan focuses on running the application directly on the Windows host while using Docker only for the database service.

#### 1. Overview
- **Hybrid Approach**: Java runs locally; PostgreSQL runs in Docker.
- **Java Version Management**: Uses **Maven Toolchains** and **Maven Enforcer Plugin** to ensure Java 21 is used, even if `JAVA_HOME` points elsewhere.
- **Fast Iteration**: Native performance for the Spring Boot application.

#### 2. Java Version Enforcement
- **Maven Toolchains**: A `toolchains-example.xml` will be provided. You map your local Java 21 path in `~/.m2/toolchains.xml`.
- **Enforcer Plugin**: The `pom.xml` will fail the build if the JDK version is not 21.

#### 3. Infrastructure (Docker Compose)
A `docker-compose.yml` in the project root defines:
- **Database Service**: `postgres:17-alpine`.
- **Persistence**: Bind-mounted or named volume for data.

#### 4. Project Structure
```text
api-service/
├── docker-compose.yml          # Just for PostgreSQL
├── toolchains-example.xml      # Template for local JDK mapping
├── src/
│   ├── main/java/net/profitwarning/api/...
│   └── main/resources/application-dev.yml
└── pom.xml                     # Includes Toolchains & Enforcer plugins
```

#### 5. Pros & Cons
| Pros | Cons |
|------|------|
| Maximum performance (no virtualization overhead) | Requires local Java 21 installation |
| Familiar workflow for most developers | Manual setup of `toolchains.xml` (once) |
| Lower resource consumption | Risk of minor OS-specific behavior differences |

#### 6. Getting Started
1. Install Java 21 locally.
2. Configure `~/.m2/toolchains.xml` using `toolchains-example.xml`.
3. Start the database: `docker-compose up -d`.
4. Run the app: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`.
