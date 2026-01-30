### Plan: Dev Containers Development Environment

This plan focuses on a fully containerized development environment using the **Dev Containers** extension in IntelliJ IDEA Ultimate.

#### 1. Overview
- **Isolation**: All tools (Java 21, Maven, PostgreSQL) run inside Docker containers.
- **Consistency**: Every developer uses the exact same environment defined in the repository.
- **Zero Local Setup**: No need to install Java or PostgreSQL on your Windows host.
- **Framework**: Spring Boot 3.5.7 with Spring Web, JPA, and Lombok.

#### 2. Infrastructure (Docker Compose)
A `docker-compose.yml` file within the `.devcontainer` folder defines:
- **App Service**:
  - Image: Based on Java 21 (Eclipse Temurin) with Maven 3.9.6.
  - Volume: Binds the project root to `/workspace`.
  - Persistence: Uses a named volume for the Maven repository (`~/.m2`).
  - Ports: 8080 exposed for the Spring Boot application.
- **Database Service**:
  - Image: `postgres:17-alpine`.
  - Persistence: Uses a named volume for database data.
  - Ports: 5432 exposed for PostgreSQL connections.

#### 3. IDE Configuration (`devcontainer.json`)
- Configures IntelliJ/VS Code to use the Docker Compose setup.
- Specifies Java 21 as the project SDK.
- Installs necessary extensions (Spring Boot, Lombok, etc.) within the container context.
- Auto-forwards ports 8080 and 5432.

#### 4. Project Structure
```text
devcontainer/
├── .devcontainer/
│   ├── devcontainer.json
│   ├── docker-compose.yml
│   └── Dockerfile
├── src/
│   └── main/
│       ├── java/net/profitwarning/api/
│       │   ├── ApiServiceApplication.java
│       │   └── controller/
│       │       └── HelloController.java
│       └── resources/
│           ├── application.yml
│           └── application-docker.yml
└── pom.xml
```

#### 5. Pros & Cons
| Pros | Cons |
|------|------|
| Guaranteed Java 21 version | Higher RAM usage due to Docker |
| Identical environment for all | Slight performance overhead on Windows (WSL2 recommended) |
| Easy onboarding | Requires IntelliJ Ultimate or VS Code |
| Spring Boot 3.5.7 with latest features | |

#### 6. Getting Started
1. Open the project in IntelliJ IDEA Ultimate or VS Code.
2. When prompted, click **"Reopen in Container"**.
3. Wait for the build to finish.
4. Run `ApiServiceApplication` directly from the IDE or use `mvn spring-boot:run`.
5. Access http://localhost:8080 to see "Hello World!".
