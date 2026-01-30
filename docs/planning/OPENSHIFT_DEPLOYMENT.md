# Red Hat OpenShift Deployment Plan

## Overview

This document outlines the complete deployment strategy for the Spring Boot + Angular full-stack application on Red Hat OpenShift Container Platform. The architecture follows cloud-native best practices with separate frontend and backend deployments.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Prerequisites](#prerequisites)
3. [Container Images](#container-images)
4. [Database & Cache Setup](#database--cache-setup)
5. [Backend Deployment](#backend-deployment)
6. [Frontend Deployment](#frontend-deployment)
7. [Networking & Routes](#networking--routes)
8. [Configuration Management](#configuration-management)
9. [CI/CD Pipeline](#cicd-pipeline)
10. [Monitoring & Observability](#monitoring--observability)
11. [Security Considerations](#security-considerations)
12. [Scaling & Performance](#scaling--performance)
13. [Backup & Disaster Recovery](#backup--disaster-recovery)

---

## Architecture Overview

### Deployment Topology

```
┌─────────────────────────────────────────────────────────────────┐
│                        OpenShift Cluster                         │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    Routes (Ingress)                        │ │
│  │  - frontend.apps.cluster.example.com                       │ │
│  │  - api.apps.cluster.example.com                            │ │
│  └──────────────┬─────────────────────────┬───────────────────┘ │
│                 │                         │                      │
│  ┌──────────────▼─────────────┐  ┌───────▼──────────────────┐  │
│  │  Frontend Service          │  │  Backend Service         │  │
│  │  (ClusterIP)               │  │  (ClusterIP)             │  │
│  └──────────────┬─────────────┘  └───────┬──────────────────┘  │
│                 │                         │                      │
│  ┌──────────────▼─────────────┐  ┌───────▼──────────────────┐  │
│  │  Frontend Deployment       │  │  Backend Deployment      │  │
│  │  ┌──────────────────────┐  │  │  ┌────────────────────┐ │  │
│  │  │ NGINX + Angular      │  │  │  │ Spring Boot JAR    │ │  │
│  │  │ (3 replicas)         │  │  │  │ (3 replicas)       │ │  │
│  │  │ Port: 8080           │  │  │  │ Port: 8080         │ │  │
│  │  └──────────────────────┘  │  │  └────────────────────┘ │  │
│  └────────────────────────────┘  └───────┬──────────────────┘  │
│                                           │                      │
│                          ┌────────────────┴────────────┐         │
│                          │                             │         │
│              ┌───────────▼──────────┐    ┌────────────▼───────┐ │
│              │  PostgreSQL          │    │  Redis             │ │
│              │  StatefulSet         │    │  Deployment        │ │
│              │  (1 replica)         │    │  (1 replica)       │ │
│              │  PVC: 20Gi           │    │  PVC: 5Gi          │ │
│              └──────────────────────┘    └────────────────────┘ │
│                                                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              ConfigMaps & Secrets                          │ │
│  │  - app-config (env vars)                                   │ │
│  │  - db-credentials (sealed secret)                          │ │
│  │  - redis-config                                            │ │
│  └────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Purpose | Scaling | Persistence |
|-----------|---------|---------|-------------|
| Frontend (NGINX) | Serve Angular static files, SPA routing | Horizontal (HPA) | No |
| Backend (Spring Boot) | REST API, business logic | Horizontal (HPA) | No |
| PostgreSQL | Primary data store | Vertical only | Yes (PVC) |
| Redis | Caching layer | Horizontal (future) | Yes (PVC) |

---

## Prerequisites

### Required Access & Tools

**OpenShift CLI:**
```bash
# Install oc CLI
curl -LO https://mirror.openshift.com/pub/openshift-v4/clients/ocp/latest/openshift-client-linux.tar.gz
tar -xzf openshift-client-linux.tar.gz
sudo mv oc kubectl /usr/local/bin/

# Verify installation
oc version
```

**Login to OpenShift:**
```bash
# Get login command from OpenShift web console
oc login --token=<your-token> --server=https://api.cluster.example.com:6443

# Create projects
oc new-project spring-angular-app-dev
oc new-project spring-angular-app-test
oc new-project spring-angular-app-prod
```

**Required Permissions:**
- Create/manage Deployments, Services, Routes
- Create PersistentVolumeClaims
- Create ConfigMaps and Secrets
- Create BuildConfigs (for CI/CD)
- Create ServiceAccounts

### Storage Classes

Verify available storage:
```bash
oc get storageclass
```

Expected output should include persistent storage (e.g., `gp3-csi`, `ocs-storagecluster-ceph-rbd`).

---

## Container Images

### Frontend Dockerfile (Multi-stage)

**Create `frontend/Dockerfile.openshift`:**
```dockerfile
# Stage 1: Build Angular application
FROM node:22-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./
RUN npm ci --only=production

# Copy source code
COPY . .

# Build for production
RUN npm run build:prod

# Stage 2: Serve with NGINX
FROM nginxinc/nginx-unprivileged:1.25-alpine

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf
COPY nginx-default.conf /etc/nginx/conf.d/default.conf

# Copy built Angular app from builder stage
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Create directory for runtime environment injection
RUN mkdir -p /usr/share/nginx/html/assets/config

# Copy environment template
COPY env.template.js /usr/share/nginx/html/assets/config/

# Use non-root user (nginx user from base image)
USER nginx

# Expose port 8080 (OpenShift requires non-privileged ports)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/ || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
```

**Create `frontend/nginx.conf`:**
```nginx
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /tmp/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    keepalive_timeout 65;
    gzip on;
    gzip_vary on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

    # Temporary directories for non-root user
    client_body_temp_path /tmp/client_temp;
    proxy_temp_path /tmp/proxy_temp_path;
    fastcgi_temp_path /tmp/fastcgi_temp;
    uwsgi_temp_path /tmp/uwsgi_temp;
    scgi_temp_path /tmp/scgi_temp;

    include /etc/nginx/conf.d/*.conf;
}
```

**Create `frontend/nginx-default.conf`:**
```nginx
server {
    listen 8080;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;

    # SPA routing - serve index.html for all routes
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Don't cache index.html
    location = /index.html {
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        expires 0;
    }

    # Health check endpoint
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }

    # Proxy API calls to backend service
    location /api/ {
        proxy_pass http://backend-service:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
}
```

**Create `frontend/env.template.js`:**
```javascript
// Runtime environment configuration
// This file is replaced with actual values during deployment
window.ENV = {
  apiUrl: '${API_URL}',
  production: ${PRODUCTION}
};
```

---

### Backend Dockerfile (Production)

**Update `Dockerfile` (already exists, enhance for OpenShift):**
```dockerfile
# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src src
RUN mvn package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for OpenShift
RUN addgroup -g 1001 -S spring && \
    adduser -u 1001 -S spring -G spring && \
    chown -R spring:spring /app

USER spring:spring

# Copy JAR from builder
COPY --from=builder --chown=spring:spring /workspace/target/api-service.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseG1GC \
  -XX:+PrintFlagsFinal"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

## Database & Cache Setup

### PostgreSQL StatefulSet

**Create `openshift/postgresql-statefulset.yaml`:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: postgresql
  labels:
    app: postgresql
spec:
  ports:
    - port: 5432
      name: postgres
  clusterIP: None
  selector:
    app: postgresql

---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgresql
spec:
  serviceName: postgresql
  replicas: 1
  selector:
    matchLabels:
      app: postgresql
  template:
    metadata:
      labels:
        app: postgresql
    spec:
      containers:
        - name: postgresql
          image: postgres:17-alpine
          ports:
            - containerPort: 5432
              name: postgres
          env:
            - name: POSTGRES_DB
              valueFrom:
                configMapKeyRef:
                  name: db-config
                  key: database-name
            - name: POSTGRES_USER
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: username
            - name: POSTGRES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: password
            - name: PGDATA
              value: /var/lib/postgresql/data/pgdata
          volumeMounts:
            - name: postgres-storage
              mountPath: /var/lib/postgresql/data
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          livenessProbe:
            exec:
              command:
                - /bin/sh
                - -c
                - pg_isready -U $POSTGRES_USER
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            exec:
              command:
                - /bin/sh
                - -c
                - pg_isready -U $POSTGRES_USER
            initialDelaySeconds: 5
            periodSeconds: 5
  volumeClaimTemplates:
    - metadata:
        name: postgres-storage
      spec:
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: 20Gi
```

---

### Redis Deployment

**Create `openshift/redis-deployment.yaml`:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: redis
  labels:
    app: redis
spec:
  ports:
    - port: 6379
      name: redis
  selector:
    app: redis

---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: redis-data
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
        - name: redis
          image: redis:7.4-alpine
          command:
            - redis-server
            - --save
            - "60"
            - "1"
            - --loglevel
            - warning
            - --requirepass
            - $(REDIS_PASSWORD)
          ports:
            - containerPort: 6379
              name: redis
          env:
            - name: REDIS_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: redis-credentials
                  key: password
          volumeMounts:
            - name: redis-storage
              mountPath: /data
          resources:
            requests:
              memory: "128Mi"
              cpu: "100m"
            limits:
              memory: "256Mi"
              cpu: "200m"
          livenessProbe:
            exec:
              command:
                - redis-cli
                - --raw
                - incr
                - ping
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            exec:
              command:
                - redis-cli
                - --raw
                - incr
                - ping
            initialDelaySeconds: 5
            periodSeconds: 5
      volumes:
        - name: redis-storage
          persistentVolumeClaim:
            claimName: redis-data
```

---

## Backend Deployment

**Create `openshift/backend-deployment.yaml`:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend-service
  labels:
    app: backend
spec:
  ports:
    - port: 8080
      targetPort: 8080
      protocol: TCP
      name: http
  selector:
    app: backend
  type: ClusterIP

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
  labels:
    app: backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
        - name: spring-boot
          image: image-registry.openshift-image-registry.svc:5000/spring-angular-app-dev/backend:latest
          ports:
            - containerPort: 8080
              protocol: TCP
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "openshift"
            - name: DATABASE_URL
              value: "jdbc:postgresql://postgresql:5432/$(DB_NAME)"
            - name: DATABASE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: username
            - name: DATABASE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-credentials
                  key: password
            - name: DB_NAME
              valueFrom:
                configMapKeyRef:
                  name: db-config
                  key: database-name
            - name: REDIS_HOST
              value: "redis"
            - name: REDIS_PORT
              value: "6379"
            - name: REDIS_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: redis-credentials
                  key: password
            - name: FRONTEND_URL
              valueFrom:
                configMapKeyRef:
                  name: app-config
                  key: frontend-url
            - name: JAVA_OPTS
              value: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
          resources:
            requests:
              memory: "512Mi"
              cpu: "500m"
            limits:
              memory: "1Gi"
              cpu: "1000m"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 10
            timeoutSeconds: 3
            failureThreshold: 3
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 5
            timeoutSeconds: 3
            failureThreshold: 3
          startupProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 0
            periodSeconds: 10
            timeoutSeconds: 3
            failureThreshold: 30
      restartPolicy: Always

---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: backend-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: backend
  minReplicas: 3
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
```

---

## Frontend Deployment

**Create `openshift/frontend-deployment.yaml`:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: frontend-service
  labels:
    app: frontend
spec:
  ports:
    - port: 8080
      targetPort: 8080
      protocol: TCP
      name: http
  selector:
    app: frontend
  type: ClusterIP

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  labels:
    app: frontend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
        - name: nginx
          image: image-registry.openshift-image-registry.svc:5000/spring-angular-app-dev/frontend:latest
          ports:
            - containerPort: 8080
              protocol: TCP
          env:
            - name: API_URL
              valueFrom:
                configMapKeyRef:
                  name: app-config
                  key: api-url
            - name: PRODUCTION
              value: "true"
          resources:
            requests:
              memory: "64Mi"
              cpu: "50m"
            limits:
              memory: "128Mi"
              cpu: "100m"
          livenessProbe:
            httpGet:
              path: /health
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /health
              port: 8080
            initialDelaySeconds: 5
            periodSeconds: 5
      restartPolicy: Always

---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: frontend-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: frontend
  minReplicas: 3
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

---

## Networking & Routes

**Create `openshift/routes.yaml`:**
```yaml
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: frontend-route
  labels:
    app: frontend
spec:
  host: app.apps.cluster.example.com
  to:
    kind: Service
    name: frontend-service
  port:
    targetPort: http
  tls:
    termination: edge
    insecureEdgeTerminationPolicy: Redirect

---
apiVersion: route.openshift.io/v1
kind: Route
metadata:
  name: backend-route
  labels:
    app: backend
spec:
  host: api.apps.cluster.example.com
  to:
    kind: Service
    name: backend-service
  port:
    targetPort: http
  tls:
    termination: edge
    insecureEdgeTerminationPolicy: Redirect
```

---

## Configuration Management

**Create `openshift/configmap.yaml`:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  frontend-url: "https://app.apps.cluster.example.com"
  api-url: "/api/v1"
  environment: "production"

---
apiVersion: v1
kind: ConfigMap
metadata:
  name: db-config
data:
  database-name: "apidb"
  database-host: "postgresql"
  database-port: "5432"
```

**Create Secrets (use Sealed Secrets in production):**
```bash
# Create database credentials secret
oc create secret generic db-credentials \
  --from-literal=username=postgres \
  --from-literal=password=$(openssl rand -base64 32)

# Create Redis credentials secret
oc create secret generic redis-credentials \
  --from-literal=password=$(openssl rand -base64 32)
```

**For production, use Sealed Secrets:**
```bash
# Install Sealed Secrets controller
oc apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.24.0/controller.yaml

# Create sealed secret
kubeseal --format=yaml < secret.yaml > sealed-secret.yaml
```

---

## CI/CD Pipeline

**Create `openshift/buildconfig-backend.yaml`:**
```yaml
apiVersion: build.openshift.io/v1
kind: BuildConfig
metadata:
  name: backend-build
spec:
  source:
    type: Git
    git:
      uri: https://github.com/your-org/your-repo.git
      ref: main
    contextDir: "."
  strategy:
    type: Docker
    dockerStrategy:
      dockerfilePath: Dockerfile
  output:
    to:
      kind: ImageStreamTag
      name: backend:latest
  triggers:
    - type: GitHub
      github:
        secret: <webhook-secret>
    - type: ConfigChange
```

**Create `openshift/buildconfig-frontend.yaml`:**
```yaml
apiVersion: build.openshift.io/v1
kind: BuildConfig
metadata:
  name: frontend-build
spec:
  source:
    type: Git
    git:
      uri: https://github.com/your-org/your-repo.git
      ref: main
    contextDir: "frontend"
  strategy:
    type: Docker
    dockerStrategy:
      dockerfilePath: Dockerfile.openshift
  output:
    to:
      kind: ImageStreamTag
      name: frontend:latest
  triggers:
    - type: GitHub
      github:
        secret: <webhook-secret>
    - type: ConfigChange
```

**Create ImageStreams:**
```yaml
apiVersion: image.openshift.io/v1
kind: ImageStream
metadata:
  name: backend
spec:
  lookupPolicy:
    local: false

---
apiVersion: image.openshift.io/v1
kind: ImageStream
metadata:
  name: frontend
spec:
  lookupPolicy:
    local: false
```

---

## Monitoring & Observability

**Enable Prometheus monitoring:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend-service
  labels:
    app: backend
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/path: "/actuator/prometheus"
    prometheus.io/port: "8080"
spec:
  # ... existing service spec
```

**Create ServiceMonitor:**
```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: backend-metrics
spec:
  selector:
    matchLabels:
      app: backend
  endpoints:
    - port: http
      path: /actuator/prometheus
      interval: 30s
```

---

## Security Considerations

### Security Context Constraints

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: app-sa

---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: app-sa-anyuid
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: system:openshift:scc:nonroot
subjects:
  - kind: ServiceAccount
    name: app-sa
```

### Network Policies

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: backend-network-policy
spec:
  podSelector:
    matchLabels:
      app: backend
  policyTypes:
    - Ingress
    - Egress
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: frontend
      ports:
        - protocol: TCP
          port: 8080
  egress:
    - to:
        - podSelector:
            matchLabels:
              app: postgresql
      ports:
        - protocol: TCP
          port: 5432
    - to:
        - podSelector:
            matchLabels:
              app: redis
      ports:
        - protocol: TCP
          port: 6379
```

---

## Scaling & Performance

### Resource Quotas

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: app-quota
spec:
  hard:
    requests.cpu: "10"
    requests.memory: "20Gi"
    limits.cpu: "20"
    limits.memory: "40Gi"
    persistentvolumeclaims: "5"
```

### PodDisruptionBudget

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: backend-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: backend

---
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: frontend-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: frontend
```

---

## Backup & Disaster Recovery

### Database Backup CronJob

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgres-backup
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: backup
              image: postgres:17-alpine
              command:
                - /bin/sh
                - -c
                - |
                  TIMESTAMP=$(date +%Y%m%d_%H%M%S)
                  pg_dump -h postgresql -U $POSTGRES_USER $POSTGRES_DB | gzip > /backups/backup_${TIMESTAMP}.sql.gz
                  # Keep only last 7 backups
                  ls -t /backups/*.sql.gz | tail -n +8 | xargs rm -f
              env:
                - name: POSTGRES_USER
                  valueFrom:
                    secretKeyRef:
                      name: db-credentials
                      key: username
                - name: POSTGRES_PASSWORD
                  valueFrom:
                    secretKeyRef:
                      name: db-credentials
                      key: password
                - name: POSTGRES_DB
                  valueFrom:
                    configMapKeyRef:
                      name: db-config
                      key: database-name
              volumeMounts:
                - name: backup-storage
                  mountPath: /backups
          restartPolicy: OnFailure
          volumes:
            - name: backup-storage
              persistentVolumeClaim:
                claimName: postgres-backup-pvc
```

---

## Deployment Checklist

### Pre-Deployment

- [ ] OpenShift cluster access configured
- [ ] Projects created (dev, test, prod)
- [ ] Container images built and pushed
- [ ] Secrets created (db-credentials, redis-credentials)
- [ ] ConfigMaps created (app-config, db-config)
- [ ] Storage classes verified
- [ ] Network policies reviewed

### Deployment Order

1. **Deploy Infrastructure:**
   ```bash
   oc apply -f openshift/configmap.yaml
   oc apply -f openshift/postgresql-statefulset.yaml
   oc apply -f openshift/redis-deployment.yaml
   ```

2. **Wait for database readiness:**
   ```bash
   oc wait --for=condition=ready pod -l app=postgresql --timeout=120s
   oc wait --for=condition=ready pod -l app=redis --timeout=60s
   ```

3. **Deploy Backend:**
   ```bash
   oc apply -f openshift/backend-deployment.yaml
   oc wait --for=condition=available deployment/backend --timeout=180s
   ```

4. **Deploy Frontend:**
   ```bash
   oc apply -f openshift/frontend-deployment.yaml
   oc wait --for=condition=available deployment/frontend --timeout=120s
   ```

5. **Create Routes:**
   ```bash
   oc apply -f openshift/routes.yaml
   ```

6. **Verify deployment:**
   ```bash
   oc get pods
   oc get routes
   curl https://app.apps.cluster.example.com/health
   curl https://api.apps.cluster.example.com/actuator/health
   ```

### Post-Deployment

- [ ] Health checks passing
- [ ] Routes accessible
- [ ] Database migrations completed
- [ ] Monitoring configured
- [ ] Backups scheduled
- [ ] Logs aggregation working
- [ ] Alerts configured
- [ ] Documentation updated

---

## Troubleshooting

### Common Issues

**Pods not starting:**
```bash
oc describe pod <pod-name>
oc logs <pod-name>
```

**Image pull failures:**
```bash
oc get events
oc describe pod <pod-name> | grep -i image
```

**Database connection issues:**
```bash
oc exec -it <backend-pod> -- sh
nc -zv postgresql 5432
```

**Performance issues:**
```bash
oc top pods
oc top nodes
oc describe hpa backend-hpa
```

---

## Summary

This deployment plan provides:
- ✅ Production-ready container images
- ✅ Separate frontend/backend deployments
- ✅ Persistent storage for PostgreSQL and Redis
- ✅ Horizontal pod autoscaling
- ✅ Health checks and readiness probes
- ✅ Secure configuration management
- ✅ CI/CD pipeline setup
- ✅ Monitoring and observability
- ✅ Backup and disaster recovery

**Estimated Deployment Time:** 2-4 hours (initial setup), 30 minutes (subsequent deployments with CI/CD)

---

**END OF PLAN_OPENSHIFT_DEPLOYMENT.md**
