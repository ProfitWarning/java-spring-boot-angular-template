# Spring Boot + Angular Full-Stack Monorepo

Modern full-stack application template with Spring Boot 3, Angular 21, PostgreSQL, and Caffeine in-memory cache.

## 🏗️ Architecture

- **Backend:** Spring Boot 3.5.9, Java 25, PostgreSQL 17, Caffeine cache
- **Frontend:** Angular 21 (standalone components), Tailwind CSS v4
- **Development:** Docker Compose with devcontainer support
- **State Management:** Angular Signals

## 📁 Project Structure

```
/
├── backend/           # Spring Boot REST API
│   ├── src/
│   │   ├── main/java/
│   │   └── test/java/
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
│
├── frontend/          # Angular SPA
│   ├── src/
│   ├── package.json
│   ├── angular.json
│   └── README.md
│
├── .devcontainer/     # Dev environment configuration
│   ├── devcontainer.json
│   ├── docker-compose.yml
│   ├── Dockerfile
│   └── start-servers.sh
│
├── LICENSE            # MIT License
├── README.md          # This file
└── package.json       # Monorepo scripts
```

## 🚀 Quick Start

### Prerequisites

- **Docker Desktop** - For running PostgreSQL
- **VS Code** (recommended) - With Dev Containers extension

### Option 1: Using Devcontainer (Recommended)

1. **Open in VS Code:**
   ```bash
   code .
   ```

2. **Reopen in Container:**
   - Click "Reopen in Container" when prompted
   - Or: `Ctrl+Shift+P` → "Dev Containers: Reopen in Container"

3. **Wait for setup to complete** (Maven dependencies will be resolved automatically)

4. **Start development servers:**
   ```bash
   npm run dev
   ```

### Option 2: Local Development

1. **Start infrastructure:**
   ```bash
   npm run docker:up
   ```

2. **Start development servers:**
   ```bash
   npm run dev
   ```

### Access Applications

- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **PostgreSQL:** localhost:5432 (user: `postgres`, password: `postgres`)

## 📜 Available Scripts

### Development

| Command | Description |
|---------|-------------|
| `npm run dev` | Start both backend and frontend servers concurrently |
| `npm run backend` | Start Spring Boot backend only |
| `npm run frontend` | Start Angular dev server only |

### Build

| Command | Description |
|---------|-------------|
| `npm run build` | Build both frontend and backend for production |
| `npm run build:frontend` | Build Angular application for production |
| `npm run build:backend` | Package Spring Boot JAR (skip tests) |

### Testing

| Command | Description |
|---------|-------------|
| `npm test` | Run all tests (frontend + backend) |
| `npm run test:frontend` | Run Angular tests in CI mode |
| `npm run test:backend` | Run Spring Boot tests with Maven |

### Code Quality

| Command | Description |
|---------|-------------|
| `npm run lint` | Lint frontend code |
| `npm run format` | Format frontend code with Prettier |

### Database

| Command | Description |
|---------|-------------|
| `npm run db:migrate` | Run Flyway database migrations |
| `npm run db:clean` | Clean database (⚠️ destructive) |
| `npm run db:info` | Show migration status |

### Docker

| Command | Description |
|---------|-------------|
| `npm run docker:up` | Start PostgreSQL container |
| `npm run docker:down` | Stop all containers |
| `npm run docker:logs` | View container logs |
| `npm run docker:restart` | Restart all containers |

### Cleanup

| Command | Description |
|---------|-------------|
| `npm run clean` | Clean all build artifacts |
| `npm run clean:backend` | Clean Maven target directory |
| `npm run clean:frontend` | Remove frontend dist and node_modules |
| `npm run clean:all` | Deep clean everything including root node_modules |

### Installation

| Command | Description |
|---------|-------------|
| `npm run install:all` | Install dependencies for both frontend and root |

## 🗄️ Database

### PostgreSQL Configuration

- **Host:** localhost
- **Port:** 5432
- **Database:** `api_db`
- **Username:** `postgres`
- **Password:** `postgres`

### Flyway Migrations

Migrations are located in `backend/src/main/resources/db/migration/`:

- `V1__Create_testmessages_table.sql` - Initial schema
- `V2__Add_created_at_to_testmessages.sql` - Add timestamp column

Run migrations with:
```bash
npm run db:migrate
```

### Caffeine Cache Configuration

- **Type:** In-memory cache (per application instance)
- **Max Size:** Configurable via `CACHE_MAX_SIZE` environment variable (default: 1000 entries)
- **TTL:** 10 minutes (expireAfterWrite)
- Used for caching message queries

## 🐳 Docker & Production Deployment

This template includes production-ready Docker configurations for local testing and deployment.

### Quick Commands

```bash
# Development mode (hot-reload)
npm run docker:up && npm run dev

# Production mode (local testing)
npm run docker:prod      # Build & run production stack
```

### What's Included

- ✅ **Production-optimized Dockerfiles** - Multi-stage builds with non-root users
- ✅ **docker-compose.prod.yml** - Complete production stack for local testing
- ✅ **Network segmentation** - Secure app-tier and db-tier networks
- ✅ **Security best practices** - Non-root containers, minimal images, health checks
- ✅ **Nginx-based serving** - Production-grade Angular delivery with gzip & caching

### Production Image Sizes (Approximate)

- **Backend:** ~200 MB (Eclipse Temurin JRE 25 Alpine)
- **Frontend:** ~25 MB (Nginx 1.29 Alpine + static assets)
- **Total:** ~225 MB

### Access Production Stack

Once running with `npm run docker:prod`:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Actuator Health: http://localhost:8080/actuator/health
- Database: Internal only (use `docker exec` for access)

### Comprehensive Documentation

For detailed information including:
- 🏗️ Building production images
- 🚀 Deployment strategies (Docker Compose, Kubernetes, Cloud platforms)
- 🔒 Security best practices and hardening
- 🔧 Customization guide (ports, nginx config, environment variables)
- 🐛 Troubleshooting common issues
- 📚 CI/CD integration examples

See **[DOCKER.md](./DOCKER.md)**

### Docker Commands Reference

```bash
# Build images
npm run docker:build              # Build all images
npm run docker:build:backend      # Backend only
npm run docker:build:frontend     # Frontend only

# Production stack management
npm run docker:prod               # Build & start (all-in-one)
npm run docker:prod:up            # Start services
npm run docker:prod:down          # Stop services
npm run docker:prod:logs          # View logs
npm run docker:prod:restart       # Restart all services
npm run docker:prod:clean         # Stop and remove volumes

# Development services (PostgreSQL, Redis)
npm run docker:up                 # Start dev services
npm run docker:down               # Stop dev services
npm run docker:logs               # View dev logs
```

### Security Features

All Docker images implement security best practices:
- ✅ **Non-root users** - Backend runs as `spring:spring`, Frontend as `angular:angular`
- ✅ **Minimal base images** - Alpine Linux for smaller attack surface
- ✅ **Multi-stage builds** - Build tools not included in final images
- ✅ **Network isolation** - Database segregated on private network
- ✅ **No hardcoded secrets** - Environment-based configuration

## 📚 Documentation

- **[Backend Documentation](./backend/README.md)** - Spring Boot API details
- **[Frontend Documentation](./frontend/README.md)** - Angular application guide

## 🛠️ Technology Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 3.5.9 | Application framework |
| Java | 25 | Programming language |
| PostgreSQL | 17 | Primary database |
| Caffeine | 3.2.3 | In-memory caching |
| Flyway | - | Database migrations |
| Maven | 3.9.6 | Build tool |

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| Angular | 21 | Frontend framework |
| TypeScript | 5.9 | Programming language |
| Tailwind CSS | 4.1.18 | Styling framework |
| RxJS | 7.8 | Reactive programming |
| Signals | - | State management |

### DevOps

| Technology | Purpose |
|------------|---------|
| Docker & Docker Compose | Containerization |
| Red Hat UBI 9 | Base container images |
| Devcontainers | Development environment |
| VS Code | IDE (recommended) |

## 🧪 API Endpoints

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| GET | `/api/v1/messages` | List all messages | - |
| GET | `/api/v1/messages/{id}` | Get message by ID | - |
| POST | `/api/v1/messages` | Create new message | `{"content": "string"}` |

### Example Usage

```bash
# Get all messages
curl http://localhost:8080/api/v1/messages

# Create a message
curl -X POST http://localhost:8080/api/v1/messages \
  -H "Content-Type: application/json" \
  -d '{"content": "Hello World"}'

# Get specific message
curl http://localhost:8080/api/v1/messages/1
```

## 🔒 CORS Configuration

Backend is configured to accept requests from:
- `http://localhost:4200` (Angular dev server)
- `http://0.0.0.0:4200` (Devcontainer access)

## ⚙️ Environment Profiles

### Backend Profiles

- **`dev`** - Development (local H2 or PostgreSQL)
- **`docker`** - Docker Compose environment
- **`openshift`** - Red Hat OpenShift deployment

Activate profile:
```bash
export SPRING_PROFILES_ACTIVE=docker
npm run backend
```

### Frontend Environments

- **Development:** `environment.development.ts`
- **Production:** `environment.production.ts`

Build for production:
```bash
npm run build:frontend
```

## 🧩 Features

### Current Features

- ✅ Full-stack monorepo structure
- ✅ Spring Boot REST API with PostgreSQL
- ✅ Caffeine in-memory caching layer
- ✅ Angular standalone components
- ✅ Tailwind CSS v4 styling
- ✅ Signal-based state management
- ✅ Flyway database migrations
- ✅ Docker Compose development environment
- ✅ Devcontainer support
- ✅ Comprehensive testing setup
- ✅ API documentation with Swagger

### Planned Features

- ⏳ Authentication & Authorization (JWT)
- ⏳ User management
- ⏳ ESLint & Prettier for frontend
- ⏳ Pre-commit hooks (Husky)
- ⏳ CI/CD with GitHub Actions
- ⏳ SonarQube integration
- ⏳ OpenShift deployment

## 🤝 Contributing

1. **Create feature branch:**
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Make changes and test:**
   ```bash
   npm test
   npm run build
   ```

3. **Commit changes:**
   ```bash
   git add .
   git commit -m "feat: add my feature"
   ```

4. **Push and create PR:**
   ```bash
   git push origin feature/my-feature
   ```

## 🐛 Troubleshooting

### Backend doesn't start

1. Check if PostgreSQL is running:
   ```bash
   npm run docker:up
   ```

2. Verify database migrations:
   ```bash
   npm run db:info
   ```

### Frontend build errors

1. Clean and reinstall:
   ```bash
   npm run clean:frontend
   cd frontend && npm install
   ```

2. Clear Angular cache:
   ```bash
   cd frontend && rm -rf .angular
   ```

### Port conflicts

If ports 8080 or 4200 are in use:
```bash
# Find and kill process on port 8080 (Windows)
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Find and kill process on port 4200
netstat -ano | findstr :4200
taskkill /PID <PID> /F
```

## 📝 Known Issues

1. **Lombok Compilation Issue:** Removed Lombok annotations from `TestMessage.java` due to circular dependency. Using manual getters/setters.

2. **Field Name Mapping:** Backend field `content` maps to database column `message` via `@Column` annotation.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Project Status:** Active Development

**Last Updated:** January 30, 2026

For detailed backend and frontend documentation, see the respective README files in `backend/` and `frontend/` directories.
