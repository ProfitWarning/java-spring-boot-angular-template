# Docker Guide

This guide explains how to build and run the application with Docker for both
development and production-like local testing. It also covers architecture,
security practices, customization, and troubleshooting.

## Goals

- Provide a production-ready Docker workflow for local testing.
- Keep images small and secure with multi-stage builds and non-root users.
- Offer a repeatable way to run the full stack (backend, frontend, database).
- Document differences between the Redis (main) and Caffeine branches.

## Quick Start (Development)

Use this flow for hot reload and fast iteration.

```bash
# Start PostgreSQL + Redis
npm run docker:up

# Start backend + frontend
npm run dev
```

Access points:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- PostgreSQL: localhost:5432
- Redis: localhost:6379

## Quick Start (Production-Like Local Testing)

Use this flow to test the production Docker images locally.

```bash
# Build images
npm run docker:build

# Start production stack
npm run docker:prod
```

Access points:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Backend Health: http://localhost:8080/actuator/health
- Frontend Health: http://localhost:4200/health
- Database: internal only (not exposed)

## Architecture Overview

### Services

| Service | Build/Image | Ports | Networks | Notes |
|--------|-------------|-------|----------|-------|
| `backend` | `./backend/Dockerfile` | `8080:8080` | `app-tier`, `db-tier` | Spring Boot API |
| `frontend` | `./frontend/Dockerfile` | `4200:8080` | `app-tier` | Nginx serving Angular |
| `db` | `postgres:17-alpine` | none | `db-tier` | PostgreSQL |
| `redis` | `redis:7.4-alpine` | none | `app-tier` | main branch only |

### Networks

- `app-tier`: Frontend ↔ Backend (and Redis on main).
- `db-tier`: Backend ↔ Database only.

### Volumes

- `spring-angular-postgres-prod`: PostgreSQL data persistence.
- `spring-angular-redis-prod`: Redis data persistence (main only).

## Docker Files and Their Roles

- `backend/Dockerfile`: Multi-stage build for the Spring Boot API.
- `backend/.dockerignore`: Reduces build context.
- `frontend/Dockerfile`: Multi-stage build for Angular + Nginx.
- `frontend/nginx.conf.template`: Runtime Nginx configuration (envsubst).
- `frontend/.dockerignore`: Reduces build context.
- `docker-compose.prod.yml`: Production-like stack for local testing.
- `.env.prod.example`: Template for environment variables.

## Building Production Images

### Build All Images

```bash
npm run docker:build
```

### Build Backend Only

```bash
npm run docker:build:backend
```

### Build Frontend Only

```bash
npm run docker:build:frontend
```

### Expected Image Sizes

- Backend: ~200 MB (Eclipse Temurin JRE 25 Alpine)
- Frontend: ~25 MB (Nginx 1.29 Alpine + static assets)
- Total: ~225 MB

## Running the Production Stack Locally

### Default (No `.env.prod`)

```bash
npm run docker:prod
```

This uses defaults defined in `docker-compose.prod.yml`.

### Using `.env.prod`

1. Copy the template:
   ```bash
   cp .env.prod.example .env.prod
   ```
2. Update credentials and hostnames.
3. Edit `docker-compose.prod.yml`:
   - Uncomment the `env_file` section.
   - Comment out the inline `environment` block.
4. Start the stack:
   ```bash
   npm run docker:prod
   ```

## Environment Variables

### Backend (Spring Boot)

| Variable | Example | Notes |
|----------|---------|------|
| `SPRING_PROFILES_ACTIVE` | `docker` | Uses `application-docker.properties` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/apidb` | Database URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `SPRING_REDIS_HOST` | `redis` | main branch only |
| `SPRING_REDIS_PORT` | `6379` | main branch only |
| `CACHE_MAX_SIZE` | `5000` | caffeine branch only |

### Frontend (Nginx + envsubst)

| Variable | Example | Notes |
|----------|---------|------|
| `NGINX_HOST` | `localhost` | Used as `server_name` |
| `BACKEND_URL` | `http://backend:8080` | API proxy target |

### Example `.env.prod`

```bash
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/apidb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=changeMeInProduction!
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
NGINX_HOST=localhost
BACKEND_URL=http://backend:8080
```

## Branch Differences (Redis vs Caffeine)

### main (Redis)

- Redis service enabled in `docker-compose.prod.yml`.
- Backend uses `spring.cache.type=redis`.
- Environment variables include `SPRING_REDIS_HOST` and `SPRING_REDIS_PORT`.

### java-25-caffeine-angular-21 (Caffeine)

- Redis service removed from `docker-compose.prod.yml`.
- Backend uses Caffeine (in-memory) caching.
- Use `CACHE_MAX_SIZE` if configured by the app.

When merging to the caffeine branch, remove:
- `redis` service section.
- `redis-prod-data` volume.
- Redis entries in backend `environment`.
- Redis entry in `depends_on`.

## Nginx Runtime Configuration

The Nginx image uses `envsubst` at container start to render
`/etc/nginx/templates/default.conf.template` into `/etc/nginx/conf.d/default.conf`.

Key template variables:
- `NGINX_HOST`
- `BACKEND_URL`

If you edit `frontend/nginx.conf.template`, rebuild the image:

```bash
npm run docker:build:frontend
```

## Health Checks

### Backend

The backend exposes a health endpoint via Spring Boot Actuator:
- `GET /actuator/health`

`docker-compose.prod.yml` uses this endpoint to monitor container health.

### Frontend

The Nginx config includes a simple health endpoint:
- `GET /health`

## Security Best Practices

- Non-root users:
  - Backend runs as `spring:spring`.
  - Frontend runs as `angular:angular`.
- Multi-stage builds reduce attack surface.
- Database is isolated on a private network.
- No secrets are baked into images.
- Default credentials are for local testing only.

## Customization Guide

### Change External Ports

Frontend:
- In `docker-compose.prod.yml`, change `"4200:8080"` to `"80:8080"`.

Backend:
- Change `"8080:8080"` to a different host port if needed.

### Expose the Database (Debugging Only)

Uncomment this in `docker-compose.prod.yml`:

```yaml
ports:
  - "5433:5432"
```

### Change Angular Dist Path

If your Angular output path differs, build with:

```bash
docker build --build-arg DIST_PATH=dist/<project> -t frontend:latest ./frontend
```

### Enable Strict Network Isolation

To prevent external access to the DB network:

```yaml
db-tier:
  internal: true
```

### Add TLS Termination

For local testing, you can add a reverse proxy (e.g., Caddy or Traefik).
For real production, terminate TLS at your load balancer or ingress.

## CI/CD Integration (Example)

The commands below are example steps you can add to a pipeline.

```bash
# Build backend image
docker build -t myregistry.com/backend:1.0.0 ./backend

# Build frontend image
docker build -t myregistry.com/frontend:1.0.0 ./frontend

# Push
docker push myregistry.com/backend:1.0.0
docker push myregistry.com/frontend:1.0.0
```

## Production Deployment Patterns

### Single VM with Docker Compose

This is the simplest production deployment strategy. Run the stack on one
server VM with a reverse proxy (Caddy, Nginx, Traefik) in front.

Example layout:

```bash
/srv/app/
  docker-compose.yml
  .env.prod
  nginx/    # optional reverse proxy config
```

Typical workflow:
- Build and push images in CI.
- Pull images on the server.
- Run `docker compose up -d`.

Example commands:

```bash
docker compose pull
docker compose up -d
docker compose ps
```

### Container Registry Workflow

Recommended steps:
- Use immutable tags (`1.0.0`, `1.0.1`) for deployments.
- Use `latest` only for local testing.
- Store secrets outside the image (Docker Secrets or vaults).

Tagging example:

```bash
docker tag spring-angular-template/backend:latest myregistry.com/backend:1.0.0
docker tag spring-angular-template/frontend:latest myregistry.com/frontend:1.0.0
```

### Kubernetes (High Level)

If you deploy to Kubernetes, create separate Deployments for backend and frontend,
and a StatefulSet or managed service for PostgreSQL.

Minimal backend Deployment (example):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
        - name: backend
          image: myregistry.com/backend:1.0.0
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: docker
```

Minimal backend Service (example):

```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend
spec:
  selector:
    app: backend
  ports:
    - port: 8080
      targetPort: 8080
```

### Managed Database

For production, prefer a managed PostgreSQL service rather than a local
container. Update `SPRING_DATASOURCE_URL` to the managed endpoint.

## Logging and Monitoring

Options to consider:
- Use `docker logs` for local debugging.
- Export logs to a central system in production (ELK, Loki, CloudWatch).
- Use Spring Boot Actuator endpoints for health and metrics.

Actuator endpoints you can enable:
- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`

## Backups and Data Safety

Recommendations:
- Use scheduled backups for PostgreSQL.
- Store backups outside the VM or cluster.
- Test restore procedures periodically.

Example backup (local container):

```bash
docker exec postgres-prod pg_dump -U postgres apidb > backup.sql
```

## Troubleshooting

### Backend container exits immediately

Actions:
- Check logs: `docker logs spring-backend-prod`
- Verify database is healthy
- Confirm `SPRING_DATASOURCE_URL` is correct

### 502 Bad Gateway from frontend

Actions:
- Verify backend is running and healthy
- Confirm `BACKEND_URL` is correct
- Check `docker network ls` and container membership

### Database connection refused

Actions:
- Wait for DB startup (10-15 seconds)
- Run: `docker exec postgres-prod pg_isready -U postgres`
- Ensure backend is on `db-tier`

### Healthcheck failures

Actions:
- Confirm `/actuator/health` is enabled
- Check backend logs for startup errors
- Increase `start_period` if necessary

### Port conflicts

Actions:
- Change host ports in `docker-compose.prod.yml`
- Stop conflicting local services

### Build context too large

Actions:
- Confirm `.dockerignore` files are present
- Avoid copying `node_modules` or `target` into images

## Cleanup and Reset

### Stop Production Stack

```bash
npm run docker:prod:down
```

### Remove Volumes (Destructive)

```bash
npm run docker:prod:clean
```

### Remove All Local Images (Optional)

```bash
docker images
docker rmi <image-id>
```

## Command Reference

### Build

```bash
npm run docker:build
npm run docker:build:backend
npm run docker:build:frontend
```

### Run

```bash
npm run docker:prod
npm run docker:prod:up
npm run docker:prod:logs
```

### Stop

```bash
npm run docker:prod:down
npm run docker:prod:clean
```

### Development Services

```bash
npm run docker:up
npm run docker:down
npm run docker:logs
```

## External References

- Dockerfile best practices: https://docs.docker.com/develop/develop-images/dockerfile_best-practices/
- Multi-stage builds: https://docs.docker.com/build/building/multi-stage/
- Docker Compose: https://docs.docker.com/compose/
- Spring Boot Docker: https://spring.io/guides/topicals/spring-boot-docker/
- Angular deployment: https://angular.dev/tools/cli/deployment
- Nginx docs: https://nginx.org/en/docs/
