# Docker Production Optimization - Implementation Plan

**Repository:** java-spring-boot-angular-template  
**Created:** January 30, 2026  
**Branch Strategy:** docker-optimization → main + java-25-caffeine-angular-21  
**Estimated Time:** ~2.5 hours  
**Status:** Ready for Implementation

---

## Quick Reference

### User Decisions Summary

| # | Decision Area | Choice | Notes |
|---|--------------|--------|-------|
| 1 | Frontend external port | **4200** | With comment showing port 80 option |
| 2 | Nginx internal port | **8080** | Non-privileged, with comment for port 80 |
| 3 | Database exposure | **None** | Commented instructions for port 5433 |
| 4 | Health checks | **docker-compose only** | Keep Dockerfiles minimal |
| 5 | Environment vars | **Hardcoded defaults** | + commented `.env` approach + example file |
| 6 | DOCKER.md depth | **Medium (350-450 lines)** | Balanced, practical |
| 7 | Testing scope | **Full testing** | Build + run + verify |
| 8 | Branch cleanup | **Keep branch** | Maintain as reference |

### File Changes Overview

**New Files (7):**
1. `frontend/Dockerfile` - Production Angular + nginx build
2. `frontend/nginx.conf.template` - Nginx configuration template (envsubst)
3. `docker-compose.prod.yml` - Production stack
4. `backend/.dockerignore` - Backend build optimization
5. `frontend/.dockerignore` - Frontend build optimization
6. `.env.prod.example` - Environment template
7. `DOCKER.md` - Comprehensive guide (366 lines)

**Modified Files (8):**
1. `backend/Dockerfile` - Fix COPY permissions bug
2. `.devcontainer/docker-compose.yml` - Add explicit network
3. `package.json` - Add production docker scripts
4. `README.md` - Add Docker section
5. `backend/README.md` - Add Docker instructions
6. `frontend/README.md` - Add Docker instructions
7. `backend/pom.xml` - Add Spring Boot Actuator dependency
8. `.gitignore` - Ignore `.env.prod`

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Branch Strategy](#branch-strategy)
3. [Phase 1: Branch Setup](#phase-1-branch-setup)
4. [Phase 2: New Files](#phase-2-new-files)
   - [File 1: frontend/Dockerfile](#file-1-frontenddockerfile)
   - [File 2: frontend/nginx.conf.template](#file-2-frontendnginxconftemplate)
   - [File 3: docker-compose.prod.yml](#file-3-docker-composeprodml)
   - [File 4: backend/.dockerignore](#file-4-backenddockerignore)
   - [File 5: frontend/.dockerignore](#file-5-frontenddockerignore)
   - [File 6: .env.prod.example](#file-6-envprodexample)
   - [File 7: DOCKER.md](#file-7-dockermd)
5. [Phase 3: File Modifications](#phase-3-file-modifications)
6. [Phase 4: Testing & Validation](#phase-4-testing--validation)
7. [Phase 5: Branch Management](#phase-5-branch-management)
8. [Implementation Checklist](#implementation-checklist)
9. [Troubleshooting Guide](#troubleshooting-guide)
10. [Success Criteria](#success-criteria)

---

## Executive Summary

### Goals

- ✅ Create production-ready Docker configurations for local testing
- ✅ Implement non-root security (users: `spring:spring`, `angular:angular`)
- ✅ Enable users to run `npm run docker:prod` for production simulation
- ✅ Provide comprehensive documentation for production deployment
- ✅ Maintain separation between `main` (Redis) and `caffeine` branches

### What Will Be Created

- **7 new files:** Dockerfiles, configs, documentation
- **6 modified files:** READMEs, docker-compose, package.json
- **1 new branch:** `docker-optimization` (kept for reference)

### Key Features

- 🔒 Non-root containers (security best practice)
- 🌐 Network segmentation (app-tier, db-tier)
- 📦 Multi-stage builds (optimized image sizes)
- 🎯 One-command deployment (`npm run docker:prod`)
- 📚 Comprehensive documentation (DOCKER.md)

### Expected Results

**Image Sizes:**
- Backend: ~200 MB (Eclipse Temurin JRE 25 Alpine)
- Frontend: ~25 MB (Nginx 1.29 Alpine)
- Total: ~225 MB

**Security:**
- All containers run as non-root users
- Database isolated on private network
- No hardcoded secrets
- Multi-stage builds minimize attack surface

---

## Branch Strategy

### Workflow Diagram

```
main (Redis variant)
  │
  └─→ docker-optimization (NEW - work here)
       │
       ├─ Create all Docker files
       ├─ Test with Redis setup
       └─ Keep as reference

After testing + manual approval (no auto-merge):
  docker-optimization
       ├─→ Manual merge to main (includes Redis) ✅
       │
       └─→ Manual merge to java-25-caffeine-angular-21
            └─ Modify docker-compose.prod.yml (remove Redis) ✅
```

**Note:** Merges to `main` and `java-25-caffeine-angular-21` are intentionally manual and should only happen after you finish testing the `docker-optimization` branch.

### Files That Will Differ Between Branches

- `.devcontainer/docker-compose.yml` - Already different (Redis vs no Redis)
- `docker-compose.prod.yml` - Will differ (Redis service in main only)
- `backend/README.md` - Already different (Redis vs Caffeine docs)

### Files That Will Be Identical

- All Dockerfiles (caching is in app code, not Docker layer)
- All .dockerignore files
- DOCKER.md (documents both approaches)
- All npm scripts

---

## Phase 1: Branch Setup

### Step 1.1: Create New Branch

```bash
# Ensure we're on main and up to date
git checkout main
git pull origin main

# Create and switch to new branch
git checkout -b docker-optimization

# Push to remote (set upstream)
git push -u origin docker-optimization

# Verify branch
git branch -vv
# Should show: * docker-optimization [origin/docker-optimization]
```

### Step 1.2: Verify Starting Point

```bash
# Check current structure
ls -la

# Verify existing Dockerfiles
ls -la backend/Dockerfile
ls -la .devcontainer/Dockerfile
ls -la .devcontainer/docker-compose.yml

# Confirm we're on the right branch
git status
# Should show: On branch docker-optimization
```

**Expected output:**
- Clean working tree
- On `docker-optimization` branch
- Synced with remote

---

## Phase 2: New Files

### File 1: `frontend/Dockerfile`

**Location:** `frontend/Dockerfile`  
**Purpose:** Production-ready Angular app with nginx serving (non-root)

**Full Contents:**

```dockerfile
# ============================================
# Stage 1: Build Angular Application
# ============================================
FROM node:24-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files for dependency caching
# This layer will be cached if package files don't change
COPY package*.json ./

# Install dependencies
# Use npm ci when package-lock.json exists, otherwise fall back to npm install
RUN if [ -f package-lock.json ]; then npm ci --silent; else npm install --silent; fi

# Copy application source code
COPY . .

# Build Angular application for production
# Uses the build:prod script from package.json
RUN npm run build:prod

# ============================================
# Stage 2: Serve with Nginx
# ============================================
FROM nginx:1.29-alpine

# Build arg for Angular dist output (default matches Angular 21 project name)
# Override with: --build-arg DIST_PATH=dist/<project>/browser
ARG DIST_PATH=dist/frontend/browser
#
# Note: This template's project name is "frontend" (see frontend/angular.json)
# and outputPath is "dist/frontend", which emits browser assets in dist/frontend/browser.

# Create non-root user for security
RUN addgroup -S angular && adduser -S angular -G angular

# Copy nginx template for envsubst
# Port 8080 (non-privileged) with Angular SPA routing support
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Copy built Angular application from build stage
# Default Angular 21 output: dist/frontend/browser
COPY --from=build --chown=angular:angular /app/${DIST_PATH} /usr/share/nginx/html

# Set permissions for nginx directories (required for non-root)
RUN chown -R angular:angular /var/cache/nginx && \
    chown -R angular:angular /var/log/nginx && \
    chown -R angular:angular /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown -R angular:angular /var/run/nginx.pid

# Switch to non-root user
USER angular:angular

# Expose port 8080 (non-privileged port)
# Change nginx.conf.template 'listen' directive to 80 if you prefer standard HTTP port (requires root or CAP_NET_BIND_SERVICE)
EXPOSE 8080

# Start nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
```

**Key Features:**
- ✅ Multi-stage build (Node builder + nginx server)
- ✅ Non-root user: `angular:angular`
- ✅ Dependency caching optimization
- ✅ Port 8080 (non-privileged, consistent with backend)
- ✅ Proper permissions for nginx directories
- ✅ Runtime config via template + envsubst (`/etc/nginx/templates`)
- ✅ Comments explaining customization options

**Estimated Image Size:** ~25 MB

---

### File 2: `frontend/nginx.conf.template`

**Location:** `frontend/nginx.conf.template`  
**Purpose:** Production nginx configuration template for Angular SPA (envsubst)

**Note:** The official Nginx image automatically runs envsubst on files in
`/etc/nginx/templates/*.template` at container start and writes the final config
to `/etc/nginx/conf.d/`. Templates must contain only a `server { ... }` block
because they are included into the existing `http` context from the base
`/etc/nginx/nginx.conf`.

**Full Contents (server block only, processed by envsubst to /etc/nginx/conf.d/default.conf):**

```nginx
server {
    # Listen on port 8080 (non-privileged)
    # This is mapped to external port 4200 in docker-compose.prod.yml
    listen 8080;
    server_name ${NGINX_HOST};

    # Root directory for static files
    root /usr/share/nginx/html;
    index index.html;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;

    # Gzip compression for faster content delivery
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript 
               application/json application/javascript application/xml+rss 
               application/rss+xml font/truetype font/opentype 
               application/vnd.ms-fontobject image/svg+xml;

    # Angular SPA routing - fallback to index.html for all routes
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy (runtime-configured)
    # BACKEND_URL is injected via envsubst at container start
    location /api/ {
        proxy_pass ${BACKEND_URL};
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # Cache static assets aggressively (JS, CSS, images, fonts)
    location ~* \.(?:css|js|jpg|jpeg|gif|png|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Don't cache index.html (ensures users get latest version)
    location = /index.html {
        add_header Cache-Control "no-cache";
        expires 0;
    }

    # Health check endpoint (for Docker healthchecks and monitoring)
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
```

**Key Features:**
- ✅ Runs as `angular` user (non-root)
- ✅ Port 8080 (non-privileged, mapped to 4200 externally)
- ✅ Angular SPA routing support (`try_files` fallback)
- ✅ Runtime-configured host via envsubst (`NGINX_HOST`)
- ✅ Runtime-configured backend URL via envsubst (`BACKEND_URL`)
- ✅ Gzip compression for performance
- ✅ Security headers (XSS, clickjacking protection)
- ✅ Aggressive caching for static assets
- ✅ No caching for index.html (ensures updates)
- ✅ Health check endpoint at `/health`

---

### File 3: `docker-compose.prod.yml`

**Location:** `docker-compose.prod.yml` (root directory)  
**Purpose:** Production stack for local testing

**Full Contents:**

```yaml
# Production Docker Compose configuration for local testing
# This simulates a production environment on your local machine
# 
# Quick start:
#   npm run docker:build      # Build images
#   npm run docker:prod:up    # Start services
#   npm run docker:prod:logs  # View logs
#   npm run docker:prod:down  # Stop services
#
# Access:
#   Frontend:  http://localhost:4200
#   Backend:   http://localhost:8080
#   Database:  Internal only (see db service for debugging access)

services:
  # ============================================
  # Backend: Spring Boot Application
  # ============================================
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: spring-backend-prod
    networks:
      - app-tier
      - db-tier
    environment:
      # Default values for local testing
      # ⚠️ CHANGE THESE FOR REAL PRODUCTION DEPLOYMENT! ⚠️
      # 
      # For production, use .env file instead:
      # 1. Copy .env.prod.example to .env.prod
      # 2. Update values with real credentials
      # 3. Uncomment 'env_file' below and comment out 'environment' section
      # 
      # env_file:
      #   - .env.prod
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/apidb
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
      # Redis configuration (main branch only)
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
      redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health >/dev/null || exit 1"]
      interval: 30s
      timeout: 5s
      retries: 3
      start_period: 40s
    restart: unless-stopped

  # ============================================
  # Frontend: Angular Application (Nginx)
  # ============================================
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: angular-frontend-prod
    networks:
      - app-tier
    ports:
      # External port 4200 (development-like) → Internal nginx port 8080
      # To use standard HTTP port 80 externally, change to: "80:8080"
      # Note: Port 80 may require sudo/admin permissions on some systems
      - "4200:8080"
    environment:
      # Runtime config via nginx envsubst templates
      # For production, move these to .env.prod and enable env_file above
      # to keep environment-specific values out of docker-compose.prod.yml
      - NGINX_HOST=localhost
      - BACKEND_URL=http://backend:8080
    depends_on:
      - backend
    restart: unless-stopped

  # ============================================
  # Database: PostgreSQL
  # ============================================
  db:
    image: postgres:17-alpine
    container_name: postgres-prod
    networks:
      - db-tier
    environment:
      - POSTGRES_DB=apidb
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    volumes:
      - postgres-prod-data:/var/lib/postgresql/data
    # Database is NOT exposed to host by default (production best practice)
    # To access with pgAdmin/DBeaver for debugging, uncomment the line below:
    # ports:
    #   - "5433:5432"  # Use different port to avoid conflict with dev database
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  # ============================================
  # Cache: Redis (Main branch only)
  # ============================================
  # Note: This service is removed in the java-25-caffeine-angular-21 branch
  redis:
    image: redis:7.4-alpine
    container_name: redis-prod
    networks:
      - app-tier
    command: ["redis-server", "--save", "60", "1", "--loglevel", "warning"]
    volumes:
      - redis-prod-data:/data
    # Redis is NOT exposed to host (only accessible by backend)
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5
    restart: unless-stopped

# ============================================
# Networks: Segmented for Security
# ============================================
networks:
  # Public network: Frontend ↔ Backend communication
  app-tier:
    name: spring-angular-app-tier
    driver: bridge
  
  # Private network: Backend ↔ Database communication
  # Database is isolated from frontend
  db-tier:
    name: spring-angular-db-tier
    driver: bridge
    # For maximum security in real production, add:
    # internal: true  # Prevents external access entirely

# ============================================
# Volumes: Persistent Data Storage
# ============================================
volumes:
  postgres-prod-data:
    name: spring-angular-postgres-prod
  redis-prod-data:
    name: spring-angular-redis-prod
```

**Key Features:**
- ✅ Port 4200 for frontend with comment for port 80
- ✅ Database not exposed (commented instructions for port 5433)
- ✅ Health checks in compose (not in Dockerfiles)
- ✅ Hardcoded env vars with comments for .env approach
- ✅ Network segmentation (app-tier, db-tier)
- ✅ Named volumes for data persistence
- ✅ Service dependencies with health conditions
- ✅ Extensive comments for user guidance

**For Caffeine Branch:** Remove the entire `redis` service section and `redis-prod-data` volume.

---

### File 4: `backend/.dockerignore`

**Location:** `backend/.dockerignore`  
**Purpose:** Optimize Docker build context for backend

**Full Contents:**

```
# Maven build output
target/
*.jar
*.war
*.ear

# IDE files
.idea/
*.iml
.vscode/
.settings/
.classpath
.project
*.swp
*.swo
*~

# Version control
.git/
.gitignore
.gitattributes

# Logs
*.log
logs/

# OS files
.DS_Store
Thumbs.db
ehthumbs.db
Desktop.ini

# Test coverage
coverage/

# Documentation (not needed in image)
*.md
docs/

# CI/CD
.github/
.gitlab-ci.yml
Jenkinsfile

# Docker files (prevent recursion)
Dockerfile
.dockerignore

# Application config (will be provided via env vars)
application-local.properties
application-dev.properties
```

---

### File 5: `frontend/.dockerignore`

**Location:** `frontend/.dockerignore`  
**Purpose:** Optimize Docker build context for frontend

**Full Contents:**

```
# Dependencies (will be installed fresh in Docker)
node_modules/

# Build output (will be built fresh in Docker)
dist/
.angular/
build/

# Testing
coverage/
*.spec.ts

# IDE files
.vscode/
.idea/
*.iml
*.swp
*.swo
*~

# Version control
.git/
.gitignore
.gitattributes

# OS files
.DS_Store
Thumbs.db
ehthumbs.db
Desktop.ini

# Logs
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# Documentation (not needed in image)
*.md
docs/

# CI/CD
.github/
.gitlab-ci.yml

# Docker files (prevent recursion)
Dockerfile
.dockerignore

# Environment files (will be built into assets)
.env.local
.env.development
.env.test
```

---

### File 6: `.env.prod.example`

**Location:** `.env.prod.example` (root directory)  
**Purpose:** Template for production environment variables

**Full Contents:**

```bash
# ============================================
# Production Environment Variables Template
# ============================================
# 
# Usage:
#   1. Copy this file: cp .env.prod.example .env.prod
#   2. Update values below with your production credentials
#   3. Add .env.prod to .gitignore (already done)
#   4. Update docker-compose.prod.yml to use env_file
#
# Security:
#   - NEVER commit .env.prod to version control
#   - Use strong, unique passwords
#   - Rotate credentials regularly
#   - Use secrets management in production (Docker Secrets, K8s Secrets, etc.)

# ============================================
# Spring Boot Configuration
# ============================================
SPRING_PROFILES_ACTIVE=docker

# ============================================
# Database Configuration
# ============================================
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/apidb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=changeMeInProduction!

# Database Pool Settings (optional)
# SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=10
# SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5

# ============================================
# Redis Configuration (Main branch only)
# ============================================
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
# SPRING_REDIS_PASSWORD=  # Uncomment if using Redis authentication

# ============================================
# Caffeine Cache Configuration (Caffeine branch only)
# ============================================
# CACHE_MAX_SIZE=5000

# ============================================
# Frontend (Nginx template)
# ============================================
NGINX_HOST=localhost
BACKEND_URL=http://backend:8080

# ============================================
# Logging Configuration
# ============================================
# LOGGING_LEVEL_ROOT=INFO
# LOGGING_LEVEL_NET_PROFITWARNING=DEBUG

# ============================================
# Application Settings
# ============================================
# SERVER_PORT=8080
# SPRING_APPLICATION_NAME=spring-angular-template

# ============================================
# Security (Optional - configure as needed)
# ============================================
# JWT_SECRET=your-secret-key-change-this
# JWT_EXPIRATION=86400000  # 24 hours in milliseconds
```

---

### File 7: `DOCKER.md`

**Location:** `DOCKER.md` (root directory)  
**Purpose:** Comprehensive Docker guide

**Note:** This is a very large file (366 lines). See the separate DOCKER.md content in the repository after implementation. The file includes:

- Quick Start (dev and prod modes)
- Development Environment documentation
- Production Docker Setup architecture
- Building Production Images guide
- Running Production Locally instructions
- Production Deployment Guide (Kubernetes, Cloud, etc.)
- Security Best Practices
- Customization Guide
- Comprehensive Troubleshooting section
- Useful Commands Reference
- Links to external resources

Due to length, the full DOCKER.md content will be provided during implementation.

---

## Phase 3: File Modifications

### Modification 1: `backend/Dockerfile`

**Location:** `backend/Dockerfile`  
**Action:** Fix COPY permissions bug

**Current Content (Lines 14-22 - BROKEN):**
```dockerfile
# Run stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Create a non-root user for compatibility with restricted container runtimes
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the jar from build stage
COPY --from=builder /workspace/target/api-service.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**⚠️ PROBLEM:** `USER spring:spring` comes BEFORE `COPY`, which will cause permission denied error.

**✅ FIXED VERSION (Replace lines 14-22 with):**
```dockerfile
# ============================================
# Run stage
# ============================================
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Install curl for container health checks
RUN apk add --no-cache curl

# Copy JAR from build stage with proper ownership
# IMPORTANT: Use --chown BEFORE switching to non-root user
COPY --from=builder --chown=spring:spring /workspace/target/*.jar app.jar

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Changes Made:**
1. ✅ Moved `COPY` before `USER` directive
2. ✅ Added `--chown=spring:spring` to COPY command
3. ✅ Changed `api-service.jar` to `*.jar` (more flexible)
4. ✅ Added section comment
5. ✅ Improved inline comments
6. ✅ Installed curl in runtime image for health checks

---

### Modification 2: `.devcontainer/docker-compose.yml`

**Location:** `.devcontainer/docker-compose.yml`  
**Action:** Add explicit named network

**Add to END of file (after volumes section):**

```yaml

# ============================================
# Networks: Explicit naming for consistency
# ============================================
networks:
  spring-angular-net:
    name: spring-angular-net
    driver: bridge
```

**Modify each service - Add `networks:` section:**

```yaml
services:
  app:
    build:
      context: ..
      dockerfile: .devcontainer/Dockerfile
    networks:
      - spring-angular-net
    # ... rest unchanged

  db:
    image: postgres:17-alpine
    networks:
      - spring-angular-net
    # ... rest unchanged

  redis:  # main branch only
    image: redis:7.4-alpine
    networks:
      - spring-angular-net
    # ... rest unchanged
```

---

### Modification 3: `package.json` (root)

**Location:** `package.json`  
**Action:** Add production Docker scripts

**Add these scripts to the `scripts` section:**

```json
{
  "scripts": {
    // ... existing scripts remain unchanged ...
    
    // ============================================
    // Production Docker Commands
    // ============================================
    "docker:build": "docker-compose -f docker-compose.prod.yml build",
    "docker:build:backend": "docker build -t spring-angular-template/backend:latest ./backend",
    "docker:build:frontend": "docker build -t spring-angular-template/frontend:latest ./frontend",
    "docker:prod:up": "docker-compose -f docker-compose.prod.yml up -d",
    "docker:prod:down": "docker-compose -f docker-compose.prod.yml down",
    "docker:prod:logs": "docker-compose -f docker-compose.prod.yml logs -f",
    "docker:prod:restart": "npm run docker:prod:down && npm run docker:prod:up",
    "docker:prod:clean": "docker-compose -f docker-compose.prod.yml down -v",
    "docker:prod": "npm run docker:build && npm run docker:prod:up",
    
    // Individual service management
    "docker:backend": "cd backend && docker build -t spring-angular-template/backend:latest .",
    "docker:frontend": "cd frontend && docker build -t spring-angular-template/frontend:latest ."
  }
}
```

---

### Modification 4: `README.md` (root)

**Location:** `README.md`  
**Action:** Add Docker section

**Insert AFTER the "Database" section (around line 200):**

```markdown
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
```

---

### Modification 5: `backend/README.md`

**Location:** `backend/README.md`  
**Action:** Add Docker section

**Insert BEFORE the "License" section:**

```markdown
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
  -e SPRING_REDIS_HOST=redis \  # main branch
  -e CACHE_MAX_SIZE=5000 \      # caffeine branch
  spring-angular-template/backend:latest
```

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
```

### Modification 7: `backend/pom.xml`

**Location:** `backend/pom.xml`  
**Action:** Add Spring Boot Actuator dependency (required for `/actuator/health`)

**Add dependency in the Spring Boot starters section:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Modification 8: `.gitignore`

**Location:** `.gitignore`  
**Action:** Ignore production env file

**Add under Environment Variables:**

```
.env.prod
```

---

### Modification 6: `frontend/README.md`

**Location:** `frontend/README.md`  
**Action:** Add Docker section

**Insert near the end of the file:**

```markdown
## 🐳 Docker

### Production Dockerfile

The frontend includes a production-ready Dockerfile with:
- Multi-stage build (Node.js builder + Nginx server)
- Non-root user (`angular:angular`)
- Optimized for size (~25 MB)
- Nginx 1.29 Alpine with production configuration

### Building the Image

```bash
# From project root (recommended)
npm run docker:build:frontend

# Or from frontend directory
cd frontend
docker build -t spring-angular-template/frontend:latest .

# Tag for registry
docker tag spring-angular-template/frontend:latest myregistry.com/frontend:1.0.0
```

### Running Standalone

```bash
# Run frontend container
docker run -d \
  --name angular-frontend \
  -p 4200:8080 \
  -e NGINX_HOST=localhost \
  -e BACKEND_URL=http://host.docker.internal:8080 \
  spring-angular-template/frontend:latest

# Access application
open http://localhost:4200

# View logs
docker logs -f angular-frontend
```

### Production Stack

For complete production environment with backend:

```bash
# From project root
npm run docker:prod

# Access application
open http://localhost:4200
```

### Nginx Configuration

The custom `nginx.conf.template` provides:
- ✅ **Angular routing support** - SPA fallback to index.html
- ✅ **Gzip compression** - Faster content delivery
- ✅ **Security headers** - XSS, clickjacking protection
- ✅ **Caching strategy** - Aggressive for assets, none for index.html
- ✅ **Health endpoint** - `/health` for monitoring
- ✅ **Port 8080** - Non-privileged (mapped to 4200 externally)
- ✅ **Runtime API target** - `BACKEND_URL` injected via envsubst

### Customizing Nginx

To modify nginx configuration:

1. Edit `frontend/nginx.conf.template` (requires rebuild)
2. Update environment variables in `docker-compose.prod.yml` (e.g., `BACKEND_URL`, `NGINX_HOST`) (no rebuild)
3. Rebuild image if template changed: `npm run docker:build:frontend`
4. Restart container

**Common customizations:**
- Change ports
- Add SSL/TLS
- Configure rate limiting
- Add custom headers
- Modify compression settings

See comments in `nginx.conf.template` for guidance.

### Build Optimization

The Dockerfile uses `.dockerignore` to exclude:
- Dependencies (`node_modules/`)
- Build output (`dist/`, `.angular/`)
- IDE files
- Test files

Build context is reduced from ~500 MB to ~5 MB, resulting in much faster builds.

### Production vs Development

| Aspect | Development (`ng serve`) | Production (Docker) |
|--------|-------------------------|---------------------|
| Server | Angular CLI dev server | Nginx |
| Port | 4200 | 8080 (mapped to 4200) |
| Build | JIT compilation | AOT compilation |
| Size | ~500 MB (with node_modules) | ~25 MB |
| Hot Reload | ✅ Yes | ❌ No (rebuild required) |
| Optimizations | ❌ Minimal | ✅ Full (minification, tree-shaking) |
| Caching | ❌ No | ✅ Yes (gzip, cache headers) |
| Security Headers | ❌ No | ✅ Yes |

### Further Documentation

See [DOCKER.md](../DOCKER.md) for comprehensive Docker documentation including:
- Production deployment strategies
- Security hardening
- CI/CD integration
- Troubleshooting
```

---

## Phase 4: Testing & Validation

### Prerequisites Check

```bash
# Verify Docker is running
docker --version
# Expected: Docker version 20.10.x or higher

# Verify Docker Compose
docker-compose --version
# Expected: Docker Compose version 2.x or higher

# Verify you're on docker-optimization branch
git branch --show-current
# Expected: docker-optimization

# Verify clean working tree
git status
# Expected: All new files created, no uncommitted modifications
```

### Test Suite Summary

| Test # | Name | Purpose | Duration |
|--------|------|---------|----------|
| 1 | Backend Build | Verify backend image builds | ~2 min |
| 2 | Frontend Build | Verify frontend image builds | ~3 min |
| 3 | Non-Root Users | Security verification | 1 min |
| 4 | Stack Startup | All services start correctly | 2 min |
| 5 | Service Health | Endpoints responding | 1 min |
| 6 | Network Segmentation | Security isolation | 2 min |
| 7 | Logs Check | No critical errors | 1 min |
| 8 | Integration Test | Full stack functionality | 2 min |
| 9 | Data Persistence | Volumes working | 3 min |
| 10 | Cleanup | Clean removal | 1 min |

**Total Testing Time:** ~20-25 minutes

### Detailed Test Procedures

For detailed step-by-step testing procedures for all 10 tests, including:
- Exact commands to run
- Expected output
- Success criteria
- Troubleshooting tips

Please refer to the main implementation plan document or execute tests during implementation phase.

**Quick Test Command Summary:**

```bash
# Test 1-2: Build images
cd backend && docker build -t test-backend:latest .
cd ../frontend && docker build -t test-frontend:latest .

# Test 3: Verify non-root
docker run --rm --entrypoint whoami test-backend  # Should output: spring
docker run --rm --entrypoint whoami test-frontend # Should output: angular

# Test 4-10: Full stack testing
cd ..
npm run docker:prod
# Wait 45 seconds for startup
curl http://localhost:8080/actuator/health
curl http://localhost:4200/health
# Create test data, verify persistence, cleanup
npm run docker:prod:clean
```

---

## Phase 5: Branch Management

### Step 5.1: Commit to docker-optimization

```bash
# Stage all new files
git add frontend/Dockerfile frontend/nginx.conf.template
git add docker-compose.prod.yml
git add backend/.dockerignore frontend/.dockerignore
git add .env.prod.example DOCKER.md

# Stage modified files
git add backend/Dockerfile .devcontainer/docker-compose.yml
git add package.json README.md
git add backend/README.md frontend/README.md

# Verify staging
git status

# Commit with comprehensive message
git commit -m "feat: Add production Docker configuration

- Add production Dockerfiles for backend and frontend
- Backend: Multi-stage build with non-root spring:spring user
- Frontend: Nginx-based serving with non-root angular:angular user
- Add docker-compose.prod.yml for local production testing
- Implement network segmentation (app-tier, db-tier)
- Add comprehensive DOCKER.md documentation (366 lines)
- Add .dockerignore files for build optimization
- Add npm scripts for production Docker workflow
- Fix backend Dockerfile COPY permissions bug
- Add explicit network naming to devcontainer compose
- Update all READMEs with Docker sections
- Add .env.prod.example template
- Add Spring Boot Actuator for health checks
- Ignore .env.prod in .gitignore

Security improvements:
- All containers run as non-root users
- Network segmentation isolates database
- Multi-stage builds minimize image size
- Health checks for monitoring
- No hardcoded secrets

Image sizes:
- Backend: ~200 MB (JRE 25 Alpine)
- Frontend: ~25 MB (Nginx 1.29 Alpine)

Tested: Full stack builds and runs successfully with data persistence"

# Push to remote
git push origin docker-optimization
```

### Manual Merge Gate

Merges to `main` and `java-25-caffeine-angular-21` are **not** automatic. Run the merge steps only after you finish testing and explicitly approve the results.

### Step 5.2: Manual Merge to Main Branch (after testing)

```bash
# Switch to main
git checkout main
git pull origin main

# Merge docker-optimization
git merge docker-optimization

# Verify Redis present
grep "redis:" docker-compose.prod.yml

# Push
git push origin main
```

### Step 5.3: Manual Merge to Caffeine Branch (after testing)

```bash
# Switch to caffeine
git checkout java-25-caffeine-angular-21
git pull origin java-25-caffeine-angular-21

# Merge
git merge docker-optimization

# IMPORTANT: Remove Redis from docker-compose.prod.yml
# Edit file and remove:
# - redis service (entire section)
# - redis-prod-data volume
# - Redis from backend depends_on
# - SPRING_REDIS_* environment variables
# Or apply the prepared patch: caffeine-compose.patch

# Commit the merge
git add docker-compose.prod.yml
git commit -m "Merge docker-optimization into caffeine branch

- Merged Docker production configuration
- Removed Redis service from docker-compose.prod.yml (Caffeine uses in-memory cache)
- Removed redis-prod-data volume
- Updated backend dependencies to remove Redis
- All other Docker features identical to main branch"

# Push
git push origin java-25-caffeine-angular-21
```

### Step 5.4: Verify All Branches

```bash
# Verify main has Redis
git checkout main
grep "redis:" docker-compose.prod.yml  # Should find Redis

# Verify caffeine has no Redis
git checkout java-25-caffeine-angular-21
grep "redis:" docker-compose.prod.yml  # Should find nothing or only comments

# Verify docker-optimization still exists
git branch -a | grep docker-optimization
```

---

## Implementation Checklist

### Phase 1: Branch Setup
- [ ] Checkout main and pull latest
- [ ] Create `docker-optimization` branch
- [ ] Push branch to remote
- [ ] Verify clean working tree

### Phase 2: New Files
- [ ] Create `backend/.dockerignore`
- [ ] Create `frontend/Dockerfile`
- [ ] Create `frontend/nginx.conf.template`
- [ ] Create `frontend/.dockerignore`
- [ ] Create `docker-compose.prod.yml`
- [ ] Create `.env.prod.example`
- [ ] Create `DOCKER.md` (366 lines)

### Phase 3: Modifications
- [ ] Fix `backend/Dockerfile` COPY bug
- [ ] Add network to `.devcontainer/docker-compose.yml`
- [ ] Add scripts to `package.json`
- [ ] Add Docker section to `README.md`
- [ ] Add Docker section to `backend/README.md`
- [ ] Add Docker section to `frontend/README.md`
- [ ] Add Spring Boot Actuator dependency to `backend/pom.xml`
- [ ] Add `.env.prod` to `.gitignore`

### Phase 4: Testing
- [ ] Test 1: Build backend image
- [ ] Test 2: Build frontend image
- [ ] Test 3: Verify non-root users
- [ ] Test 4: Run production stack
- [ ] Test 5: Verify service health
- [ ] Test 6: Verify network segmentation
- [ ] Test 7: Check logs
- [ ] Test 8: Integration test
- [ ] Test 9: Data persistence
- [ ] Test 10: Cleanup

### Phase 5: Branch Management
- [ ] Review all changes
- [ ] Commit to `docker-optimization`
- [ ] Push `docker-optimization`
- [ ] Confirm tests passed and approve manual merges
- [ ] Merge to `main` with Redis
- [ ] Push `main`
- [ ] Merge to `java-25-caffeine-angular-21`
- [ ] Remove Redis from caffeine compose
- [ ] Push `java-25-caffeine-angular-21`
- [ ] Verify all branches

---

## Troubleshooting Guide

### Build Issues

**"COPY failed: no source files found"**
- Check `.dockerignore` isn't excluding required files
- Verify build context path is correct

**"npm ERR! code ENOENT package.json"**
- Verify COPY order in Dockerfile (package files first)
- Check `.dockerignore` doesn't exclude package.json

**"Permission denied" during COPY**
- Ensure `--chown=spring:spring` in COPY directive
- Verify USER directive comes AFTER COPY

### Runtime Issues

**Backend container exits immediately**
- Check logs: `docker logs spring-backend-prod`
- Verify database health check in depends_on
- Check environment variables match database

**"502 Bad Gateway" from frontend**
- Verify backend is running and healthy
- Check both services on app-tier network
- Test backend: `curl http://localhost:8080/actuator/health`

**Database connection refused**
- Wait longer (database may need 10-15 seconds)
- Check database health: `docker exec postgres-prod pg_isready -U postgres`
- Verify backend and db on db-tier network

### Network Issues

**Services can't communicate**
- Verify networks created: `docker network ls`
- Check service network membership: `docker inspect <container> | grep Networks`
- Recreate stack: `npm run docker:prod:down && npm run docker:prod:up`

**Frontend CAN access database (shouldn't be possible)**
- Verify frontend only on app-tier (not db-tier)
- Check docker-compose.prod.yml networks configuration

### Permission Issues

**Nginx fails with permission errors**
- Verify permissions set for nginx directories in Dockerfile
- Check USER directive comes after chown commands
- Review nginx error log: `docker logs angular-frontend-prod`

### Data Persistence Issues

**Data lost after restart**
- Verify volumes defined in docker-compose.prod.yml
- Check volumes exist: `docker volume ls | grep spring-angular`
- Use named volumes, not anonymous

### For More Issues

See detailed troubleshooting section in DOCKER.md with solutions for:
- Port conflicts
- Disk space issues
- Build cache problems
- Merge conflicts
- Testing failures

---

## Success Criteria

### Functional Requirements
- ✅ Backend Docker image builds without errors
- ✅ Frontend Docker image builds without errors
- ✅ `npm run docker:prod` starts all services
- ✅ Frontend accessible at http://localhost:4200
- ✅ Backend API accessible at http://localhost:8080
- ✅ Database initialized and accessible
- ✅ Redis working (main) / Caffeine working (caffeine)
- ✅ Data persists across container restarts
- ✅ Full integration test passes

### Security Requirements
- ✅ Backend runs as non-root user (spring:spring)
- ✅ Frontend runs as non-root user (angular:angular)
- ✅ Database isolated on separate network
- ✅ Frontend cannot directly access database
- ✅ No secrets hardcoded in images
- ✅ Health checks implemented

### Quality Requirements
- ✅ Image sizes reasonable (~200MB backend, ~25MB frontend)
- ✅ Build times acceptable (<2 min backend, <3 min frontend)
- ✅ No critical errors in logs
- ✅ Documentation complete and accurate
- ✅ All README files updated

### Branch Requirements
- ✅ `docker-optimization` branch created and contains all work
- ✅ Merged to `main` with Redis
- ✅ Merged to `java-25-caffeine-angular-21` without Redis
- ✅ Both branches tested and working
- ✅ All branches pushed to remote
- ✅ `docker-optimization` branch kept for reference

---

## Timeline

| Phase | Tasks | Estimated Time |
|-------|-------|----------------|
| Phase 1 | Branch setup | 5 minutes |
| Phase 2 | Create 7 new files | 30 minutes |
| Phase 3 | Modify 6 files | 20 minutes |
| Phase 4 | Testing (10 tests) | 45 minutes |
| Phase 5 | Branch management | 20 minutes |
| Review | Final verification | 15 minutes |

**Total Estimated Time:** ~2.5 hours

---

## Additional Notes

### Main Branch vs Caffeine Branch

| Aspect | Main | Caffeine |
|--------|------|----------|
| Cache | Redis 7.4 | Caffeine (in-memory) |
| Services | 4 containers | 3 containers |
| docker-compose.prod.yml | Has redis | No redis |
| Volumes | postgres + redis | postgres only |
| Env vars | SPRING_REDIS_* | CACHE_MAX_SIZE |

### Key Technical Details

**Backend Dockerfile:**
- Base: `maven:3.9-eclipse-temurin-25-alpine` (build)
- Runtime: `eclipse-temurin:25-jre-alpine`
- User: `spring:spring`
- Port: 8080

**Frontend Dockerfile:**
- Base: `node:24-alpine` (build)
- Runtime: `nginx:1.29-alpine`
- User: `angular:angular`
- Port: 8080 (internal), 4200 (external)

**Networks:**
- `spring-angular-app-tier` - Frontend, Backend, Redis
- `spring-angular-db-tier` - Backend, Database only

**Volumes:**
- `spring-angular-postgres-prod` - Database data
- `spring-angular-redis-prod` - Redis data (main only)

---

## Reference Links

- [Docker Best Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
- [Multi-stage Builds](https://docs.docker.com/build/building/multi-stage/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)
- [Angular Deployment](https://angular.dev/tools/cli/deployment)
- [Nginx Configuration](https://nginx.org/en/docs/)

---

## Document Status

**Version:** 1.0  
**Last Updated:** January 30, 2026  
**Status:** Ready for Implementation  
**Next Action:** Create `docker-optimization` branch and begin Phase 1 (merges to `main` and `java-25-caffeine-angular-21` are manual after testing)

---

**END OF IMPLEMENTATION PLAN**

When ready to execute this plan, start with Phase 1: Branch Setup.
