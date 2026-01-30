# Angular Frontend Integration Plan

## Project Overview

Integration of Angular 19 frontend into existing Spring Boot 3.5.9 monorepo template with PostgreSQL and Redis. This plan details the complete refactoring needed to support full-stack development within a single devcontainer environment.

## Current State

**Backend:**
- Spring Boot 3.5.9 with Java 21
- Maven 3.9.6 build system
- PostgreSQL 17 database
- Redis 7.4 for caching
- Flyway migrations
- SpringDoc OpenAPI/Swagger
- REST API at `/api/v1/messages`

**Infrastructure:**
- Devcontainer with Docker Compose
- Ports: 8080 (Spring Boot), 5432 (PostgreSQL), 6379 (Redis)
- Java-focused VS Code extensions
- Eclipse Temurin JDK 21 base image

## Target Architecture

### Repository Structure
```
with_angular/
├── .devcontainer/
│   ├── devcontainer.json          # Updated with Angular tooling
│   ├── docker-compose.yml          # Updated with Node services
│   └── Dockerfile                  # Updated with Node.js 22 LTS
├── .vscode/
│   └── tasks.json                  # NEW - Auto-start tasks
├── frontend/                       # NEW - Angular 19 application
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   ├── models/
│   │   │   ├── services/
│   │   │   └── signals/           # Signals-based state management
│   │   ├── assets/
│   │   ├── environments/
│   │   └── styles/                # Tailwind CSS
│   ├── angular.json
│   ├── package.json
│   ├── tailwind.config.js
│   ├── tsconfig.json
│   └── proxy.conf.json            # Dev proxy to backend
├── src/                            # Existing Spring Boot
│   └── main/
│       ├── java/
│       │   └── net/profitwarning/api/
│       │       ├── config/
│       │       │   └── WebConfig.java    # NEW - CORS configuration
│       │       └── ...
│       └── resources/
│           └── application-openshift.properties  # NEW - Production profile
├── package.json                    # NEW - Root workspace scripts
├── pom.xml                         # Existing (minimal changes)
├── README.md                       # NEW - Complete documentation
└── .gitignore                      # Updated for Node/Angular
```

### Technology Stack

**Frontend:**
- Angular 19 (standalone components)
- TypeScript 5.7.x
- Tailwind CSS 3.x
- Angular Signals (state management)
- RxJS 7.8.x
- Angular HttpClient

**Build Tools:**
- npm/node 22 LTS
- Angular CLI 19
- concurrently (parallel script execution)

**Development:**
- Port 4200: Angular dev server (hot reload)
- Port 8080: Spring Boot API
- Proxy: `/api/**` → `http://localhost:8080`

---

## Implementation Phases

### Phase 1: Devcontainer Infrastructure Updates

**Estimated Time:** 1-2 hours

#### 1.1 Update `.devcontainer/Dockerfile`

**Current File:** Eclipse Temurin 21 with Java tooling only

**Required Changes:**
```dockerfile
FROM eclipse-temurin:21-jdk

# [Keep existing apt-get installations]

# Install Maven
ARG MAVEN_VERSION=3.9.6
# [Keep existing Maven installation]

# === NEW: Install Node.js 22 LTS ===
ARG NODE_VERSION=22
RUN curl -fsSL https://deb.nodesource.com/setup_${NODE_VERSION}.x | bash - \
    && apt-get install -y nodejs \
    && npm install -g @angular/cli@19 \
    && npm install -g npm@latest \
    && rm -rf /var/lib/apt/lists/*

# Verify installations
RUN node --version && npm --version && ng version

# [Keep existing tools: fd, ripgrep, neovim, etc.]

WORKDIR /workspace
CMD ["sleep", "infinity"]
```

**Validation:**
- `node --version` returns v22.x.x
- `npm --version` returns 10.x.x
- `ng version` returns Angular CLI 19.x.x

---

#### 1.2 Update `.devcontainer/docker-compose.yml`

**Current:** Exposes ports 8080, 5432, 6379

**Required Changes:**
```yaml
version: '3.8'

services:
  app:
    build:
      context: ..
      dockerfile: .devcontainer/Dockerfile
    volumes:
      - ..:/workspace:cached
      - maven-repo:/root/.m2
      - node-modules:/workspace/node_modules        # NEW - npm cache
      - angular-node-modules:/workspace/frontend/node_modules  # NEW
      - intellij-settings:/root/.IntelliJIdea
      - vscode-server:/root/.vscode-server
      - vscode-server-insiders:/root/.vscode-server-insiders
      - bash-history:/root/history
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/apidb
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
      # NEW - Angular dev server configuration
      - NG_CLI_ANALYTICS=false
    ports:
      - "8080:8080"    # Spring Boot
      - "4200:4200"    # NEW - Angular dev server
    depends_on:
      - db
      - redis
    command: sleep infinity

  db:
    # [Keep existing PostgreSQL configuration]

  redis:
    # [Keep existing Redis configuration]

volumes:
  maven-repo:
  node-modules:              # NEW
  angular-node-modules:      # NEW
  postgres-data:
  redis-data:
  intellij-settings:
  vscode-server:
  vscode-server-insiders:
  bash-history:
```

**Rationale for node_modules volumes:**
- Improves performance on Windows/Mac with Docker Desktop
- Prevents permission issues
- Faster installation and rebuild times

---

#### 1.3 Update `.devcontainer/devcontainer.json`

**Required Changes:**
```json
{
  "name": "Spring-Boot + Angular Dev Container",
  "dockerComposeFile": "docker-compose.yml",
  "service": "app",
  "workspaceFolder": "/workspace",
  "shutdownAction": "stopCompose",
  "customizations": {
    "vscode": {
      "extensions": [
        // Existing Java extensions
        "vscjava.vscode-java-pack",
        "vmware.vscode-spring-boot",
        "vscjava.vscode-spring-initializr",
        "vscjava.vscode-spring-boot-dashboard",
        "GabrielBB.vscode-lombok",
        "vscjava.vscode-java-test",
        "vscjava.vscode-java-debug",
        "vscjava.vscode-maven",
        "redhat.vscode-xml",
        "redhat.vscode-yaml",
        
        // NEW - Angular/TypeScript extensions
        "angular.ng-template",
        "johnpapa.angular2",
        "dbaeumer.vscode-eslint",
        "esbenp.prettier-vscode",
        "christian-kohler.path-intellisense",
        "bradlc.vscode-tailwindcss",
        
        // Existing tools
        "ms-azuretools.vscode-docker",
        "mtxr.sqltools",
        "mtxr.sqltools-driver-pg",
        "eamodio.gitlens",
        "humao.rest-client",
        "GitHub.copilot",
        "GitHub.copilot-chat"
      ],
      "settings": {
        "java.jdt.ls.java.home": "/opt/java/openjdk",
        "java.configuration.runtimes": [
          {
            "name": "JavaSE-21",
            "path": "/opt/java/openjdk",
            "default": true
          }
        ],
        // NEW - TypeScript/Angular settings
        "typescript.tsdk": "frontend/node_modules/typescript/lib",
        "editor.formatOnSave": true,
        "editor.defaultFormatter": "esbenp.prettier-vscode",
        "[typescript]": {
          "editor.defaultFormatter": "esbenp.prettier-vscode"
        },
        "[html]": {
          "editor.defaultFormatter": "esbenp.prettier-vscode"
        },
        "tailwindCSS.experimental.classRegex": [
          ["class:\\s*?[\"'`]([^\"'`]*).*?[\"'`]", "[\"'`]([^\"'`]*).*?[\"'`]"]
        ]
      }
    },
    "jetbrains": {
      "plugins": [
        // Keep existing Java plugins
        "org.jetbrains.plugins.spring",
        "org.jetbrains.plugins.spring.boot",
        "org.mapstruct.intellij",
        "Lombok Plugin",
        "com.intellij.spring.data",
        "com.dubreuia.save.actions",
        "CheckStyle-IDEA",
        "com.github.copilot",
        "com.intellij.ai.assistant",
        "com.github.junie-ai",
        // NEW - Angular plugins
        "AngularJS",
        "JavaScriptDebugger",
        "com.intellij.css"
      ]
    }
  },
  "forwardPorts": [8080, 4200, 5432, 6379],
  "postCreateCommand": "mvn dependency:resolve && npm install && cd frontend && npm install",
  "postStartCommand": "bash .devcontainer/start-servers.sh &",
  "remoteUser": "root"
}
```

**Key Changes:**
- Added Angular/TypeScript extensions
- Updated `postCreateCommand` to install npm dependencies
- Added `postStartCommand` to auto-start servers (we'll create this script)
- Updated ports to include 4200

---

#### 1.4 Create `.devcontainer/start-servers.sh`

**NEW FILE** - Auto-start script for both servers

```bash
#!/bin/bash
# start-servers.sh - Automatically start Spring Boot and Angular dev servers

set -e

echo "🚀 Starting development servers..."

# Function to wait for Spring Boot to be ready
wait_for_spring_boot() {
    echo "⏳ Waiting for Spring Boot to start..."
    while ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; do
        sleep 2
    done
    echo "✅ Spring Boot is ready!"
}

# Start Spring Boot in background
echo "🟢 Starting Spring Boot backend..."
cd /workspace
mvn spring-boot:run > /tmp/spring-boot.log 2>&1 &
SPRING_PID=$!

# Wait for Spring Boot
wait_for_spring_boot

# Start Angular dev server
echo "🔵 Starting Angular frontend..."
cd /workspace/frontend
npm start > /tmp/angular.log 2>&1 &
ANGULAR_PID=$!

echo ""
echo "✅ All servers started successfully!"
echo ""
echo "📍 Access URLs:"
echo "   - Frontend:  http://localhost:4200"
echo "   - Backend:   http://localhost:8080"
echo "   - Swagger:   http://localhost:8080/swagger-ui.html"
echo "   - Database:  localhost:5432 (postgres/postgres)"
echo "   - Redis:     localhost:6379"
echo ""
echo "📋 Logs:"
echo "   - Spring Boot: tail -f /tmp/spring-boot.log"
echo "   - Angular:     tail -f /tmp/angular.log"
echo ""

# Keep script running
wait
```

**Make executable:**
```bash
chmod +x .devcontainer/start-servers.sh
```

---

#### 1.5 Create `.vscode/tasks.json`

**NEW FILE** - VS Code tasks for manual server control

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Start Backend",
      "type": "shell",
      "command": "mvn spring-boot:run",
      "isBackground": true,
      "problemMatcher": {
        "pattern": {
          "regexp": "^([^\\s].*):(\\d+):\\s+(warning|error):\\s+(.*)$",
          "file": 1,
          "line": 2,
          "severity": 3,
          "message": 4
        },
        "background": {
          "activeOnStart": true,
          "beginsPattern": "^.*Tomcat started on port.*$",
          "endsPattern": "^.*Started ApiServiceApplication in.*$"
        }
      },
      "presentation": {
        "group": "servers",
        "panel": "dedicated"
      }
    },
    {
      "label": "Start Frontend",
      "type": "shell",
      "command": "cd frontend && npm start",
      "isBackground": true,
      "problemMatcher": {
        "pattern": {
          "regexp": "^([^\\s].*):(\\d+):(\\d+)\\s+-\\s+(warning|error)\\s+(.*)$",
          "file": 1,
          "line": 2,
          "column": 3,
          "severity": 4,
          "message": 5
        },
        "background": {
          "activeOnStart": true,
          "beginsPattern": "^.*webpack.*compiled.*$",
          "endsPattern": "^.*Compiled successfully.*$"
        }
      },
      "presentation": {
        "group": "servers",
        "panel": "dedicated"
      }
    },
    {
      "label": "Start All Servers",
      "dependsOn": ["Start Backend", "Start Frontend"],
      "problemMatcher": []
    },
    {
      "label": "Build Frontend",
      "type": "shell",
      "command": "cd frontend && npm run build",
      "group": "build",
      "presentation": {
        "panel": "dedicated"
      }
    },
    {
      "label": "Build Backend",
      "type": "shell",
      "command": "mvn clean package",
      "group": "build",
      "presentation": {
        "panel": "dedicated"
      }
    },
    {
      "label": "Build All",
      "dependsOn": ["Build Frontend", "Build Backend"],
      "group": {
        "kind": "build",
        "isDefault": true
      }
    },
    {
      "label": "Test Frontend",
      "type": "shell",
      "command": "cd frontend && npm test",
      "group": "test"
    },
    {
      "label": "Test Backend",
      "type": "shell",
      "command": "mvn test",
      "group": "test"
    }
  ]
}
```

**Phase 1 Validation Checklist:**
- [ ] Devcontainer rebuilds successfully
- [ ] `node --version` shows v22.x.x
- [ ] `ng version` shows Angular CLI 19.x.x
- [ ] Ports 8080, 4200, 5432, 6379 are forwarded
- [ ] All VS Code extensions are installed
- [ ] Maven dependencies resolve
- [ ] npm dependencies install (after Phase 2)

---

### Phase 2: Angular Application Setup

**Estimated Time:** 2-3 hours

#### 2.1 Create Angular 19 Application

**Location:** `/frontend`

**Commands:**
```bash
cd /workspace
ng new frontend \
  --routing=true \
  --style=css \
  --standalone=true \
  --skip-git=true \
  --package-manager=npm
```

**Configuration choices:**
- Standalone components: Yes (Angular 19 default)
- Routing: Yes
- Stylesheet format: CSS (we'll add Tailwind)
- SSR: No (separate frontend deployment)

**Expected structure:**
```
frontend/
├── src/
│   ├── app/
│   │   ├── app.component.ts
│   │   ├── app.config.ts
│   │   └── app.routes.ts
│   ├── assets/
│   ├── index.html
│   ├── main.ts
│   └── styles.css
├── angular.json
├── package.json
├── tsconfig.json
└── tsconfig.app.json
```

---

#### 2.2 Configure Tailwind CSS

**Install dependencies:**
```bash
cd frontend
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init
```

**Update `frontend/tailwind.config.js`:**
```javascript
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
```

**Update `frontend/src/styles.css`:**
```css
@tailwind base;
@tailwind components;
@tailwind utilities;

/* Custom global styles */
body {
  @apply bg-gray-50 text-gray-900;
}
```

---

#### 2.3 Configure Development Proxy

**Create `frontend/proxy.conf.json`:**
```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "logLevel": "debug",
    "changeOrigin": true
  }
}
```

**Update `frontend/angular.json`:**
Find the `serve` configuration and add `proxyConfig`:
```json
{
  "projects": {
    "frontend": {
      "architect": {
        "serve": {
          "options": {
            "proxyConfig": "proxy.conf.json",
            "port": 4200,
            "host": "0.0.0.0"
          }
        }
      }
    }
  }
}
```

**Rationale:**
- All `/api/**` requests from Angular → proxied to Spring Boot
- Avoids CORS issues in development
- Simulates production routing

---

#### 2.4 Configure Environments

**Create `frontend/src/environments/environment.ts`:**
```typescript
export const environment = {
  production: false,
  apiUrl: '/api/v1', // Proxied in dev, direct in prod
};
```

**Create `frontend/src/environments/environment.development.ts`:**
```typescript
export const environment = {
  production: false,
  apiUrl: '/api/v1',
  enableDebugTools: true,
};
```

**Create `frontend/src/environments/environment.production.ts`:**
```typescript
export const environment = {
  production: true,
  apiUrl: '/api/v1', // Will be served from same origin in OpenShift
};
```

**Update `frontend/angular.json` to use file replacements:**
Find the `configurations` section and ensure it includes:
```json
{
  "configurations": {
    "production": {
      "fileReplacements": [
        {
          "replace": "src/environments/environment.ts",
          "with": "src/environments/environment.production.ts"
        }
      ]
    },
    "development": {
      "fileReplacements": [
        {
          "replace": "src/environments/environment.ts",
          "with": "src/environments/environment.development.ts"
        }
      ]
    }
  }
}
```

---

#### 2.5 Setup ESLint and Prettier

**Install dependencies:**
```bash
cd frontend
ng add @angular-eslint/schematics
npm install -D prettier eslint-config-prettier eslint-plugin-prettier
```

**Create `frontend/.eslintrc.json`:**
```json
{
  "root": true,
  "overrides": [
    {
      "files": ["*.ts"],
      "extends": [
        "eslint:recommended",
        "plugin:@typescript-eslint/recommended",
        "plugin:@angular-eslint/recommended",
        "plugin:@angular-eslint/template/process-inline-templates",
        "prettier"
      ],
      "rules": {
        "@angular-eslint/directive-selector": [
          "error",
          {
            "type": "attribute",
            "prefix": "app",
            "style": "camelCase"
          }
        ],
        "@angular-eslint/component-selector": [
          "error",
          {
            "type": "element",
            "prefix": "app",
            "style": "kebab-case"
          }
        ]
      }
    },
    {
      "files": ["*.html"],
      "extends": [
        "plugin:@angular-eslint/template/recommended",
        "plugin:@angular-eslint/template/accessibility"
      ]
    }
  ]
}
```

**Create `frontend/.prettierrc.json`:**
```json
{
  "semi": true,
  "singleQuote": true,
  "trailingComma": "es5",
  "printWidth": 100,
  "tabWidth": 2,
  "useTabs": false,
  "bracketSpacing": true,
  "arrowParens": "avoid"
}
```

**Create `frontend/.prettierignore`:**
```
dist/
node_modules/
coverage/
.angular/
```

---

#### 2.6 Update `frontend/package.json` scripts

Add/update scripts section:
```json
{
  "scripts": {
    "ng": "ng",
    "start": "ng serve --host 0.0.0.0",
    "build": "ng build",
    "build:prod": "ng build --configuration production",
    "watch": "ng build --watch --configuration development",
    "test": "ng test",
    "test:ci": "ng test --watch=false --browsers=ChromeHeadless",
    "lint": "ng lint",
    "lint:fix": "ng lint --fix",
    "format": "prettier --write \"src/**/*.{ts,html,css,json}\""
  }
}
```

**Phase 2 Validation Checklist:**
- [ ] `ng serve` starts successfully on port 4200
- [ ] Tailwind CSS classes work in components
- [ ] Proxy configuration forwards `/api` to `localhost:8080`
- [ ] ESLint runs without errors
- [ ] Prettier formats files correctly
- [ ] Environment files are properly configured

---

### Phase 3: Root Build Coordination

**Estimated Time:** 30 minutes

#### 3.1 Create Root `package.json`

**NEW FILE:** `/package.json`

```json
{
  "name": "spring-boot-angular-template",
  "version": "0.0.1",
  "description": "Full-stack template with Spring Boot, Angular, PostgreSQL, and Redis",
  "private": true,
  "scripts": {
    "frontend": "cd frontend && npm start",
    "backend": "mvn spring-boot:run",
    "dev": "concurrently --kill-others \"npm run backend\" \"npm run frontend\"",
    "build": "npm run build:frontend && npm run build:backend",
    "build:frontend": "cd frontend && npm run build:prod",
    "build:backend": "mvn clean package -DskipTests",
    "test": "npm run test:frontend && npm run test:backend",
    "test:frontend": "cd frontend && npm run test:ci",
    "test:backend": "mvn test",
    "lint": "npm run lint:frontend",
    "lint:frontend": "cd frontend && npm run lint",
    "format": "cd frontend && npm run format",
    "clean": "mvn clean && rm -rf frontend/dist frontend/.angular frontend/node_modules node_modules",
    "install:all": "npm install && cd frontend && npm install"
  },
  "devDependencies": {
    "concurrently": "^8.2.2"
  },
  "engines": {
    "node": ">=22.0.0",
    "npm": ">=10.0.0"
  },
  "workspaces": [
    "frontend"
  ]
}
```

**Install root dependencies:**
```bash
cd /workspace
npm install
```

---

#### 3.2 Create `.editorconfig`

**NEW FILE:** `/.editorconfig`

```ini
root = true

[*]
charset = utf-8
indent_style = space
indent_size = 2
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.java]
indent_size = 4

[*.md]
trim_trailing_whitespace = false

[*.{yml,yaml}]
indent_size = 2

[Makefile]
indent_style = tab
```

**Phase 3 Validation Checklist:**
- [ ] `npm run dev` starts both servers
- [ ] `npm run build` builds both projects
- [ ] `npm test` runs all tests
- [ ] Root package.json scripts work correctly

---

### Phase 4: Backend Integration

**Estimated Time:** 1 hour

#### 4.1 Create CORS Configuration

**NEW FILE:** `src/main/java/net/profitwarning/api/config/WebConfig.java`

```java
package net.profitwarning.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins.split(","))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
```

---

#### 4.2 Update Application Properties

**Update `src/main/resources/application-dev.properties`:**
Add:
```properties
cors.allowed-origins=http://localhost:4200
```

**Update `src/main/resources/application-docker.properties`:**
Add:
```properties
cors.allowed-origins=http://localhost:4200
```

**Create `src/main/resources/application-openshift.properties`:**
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT:6379}

cors.allowed-origins=${FRONTEND_URL}

# Actuator endpoints for health checks
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.health.readiness-state.enabled=true
management.health.liveness-state.enabled=true
```

---

#### 4.3 Verify API Endpoints

**Current controller:** `MessageController.java` already uses `/api/v1/messages`

**Validation:**
- All REST controllers should use `/api/v1/**` prefix
- OpenAPI docs available at `/swagger-ui.html`
- Actuator health endpoint at `/actuator/health`

---

#### 4.4 Add Health Endpoint for Frontend

**Optional:** Create a dedicated health endpoint for frontend checks

**Create `src/main/java/net/profitwarning/api/controller/HealthController.java`:**
```java
package net.profitwarning.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "api-service"
        ));
    }
}
```

**Phase 4 Validation Checklist:**
- [ ] CORS allows requests from `localhost:4200`
- [ ] All API endpoints are under `/api/v1/**`
- [ ] Health endpoints respond correctly
- [ ] OpenShift properties file created

---

### Phase 5: Frontend Implementation

**Estimated Time:** 3-4 hours

#### 5.1 Create TypeScript Models

**Create `frontend/src/app/models/message.model.ts`:**
```typescript
export interface Message {
  id: number;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateMessageCommand {
  content: string;
}

export interface MessageResponse {
  id: number;
  content: string;
  createdAt: string;
  updatedAt: string;
}
```

---

#### 5.2 Create Message Service with HttpClient

**Create `frontend/src/app/services/message.service.ts`:**
```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Message, CreateMessageCommand, MessageResponse } from '../models/message.model';

@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/messages`;

  getMessages(): Observable<MessageResponse[]> {
    return this.http.get<MessageResponse[]>(this.apiUrl).pipe(
      retry(2),
      catchError(this.handleError)
    );
  }

  getMessageById(id: number): Observable<MessageResponse> {
    return this.http.get<MessageResponse>(`${this.apiUrl}/${id}`).pipe(
      retry(2),
      catchError(this.handleError)
    );
  }

  createMessage(command: CreateMessageCommand): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.apiUrl, command).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
```

---

#### 5.3 Create Signals-Based State Management

**Create `frontend/src/app/signals/message.signals.ts`:**
```typescript
import { Injectable, signal, computed, effect } from '@angular/core';
import { MessageResponse } from '../models/message.model';
import { environment } from '../../environments/environment';

export interface MessageState {
  messages: MessageResponse[];
  loading: boolean;
  error: string | null;
  selectedMessage: MessageResponse | null;
}

@Injectable({
  providedIn: 'root'
})
export class MessageSignals {
  // State signals
  private readonly messagesSignal = signal<MessageResponse[]>([]);
  private readonly loadingSignal = signal<boolean>(false);
  private readonly errorSignal = signal<string | null>(null);
  private readonly selectedMessageSignal = signal<MessageResponse | null>(null);

  // Public read-only signals
  readonly messages = this.messagesSignal.asReadonly();
  readonly loading = this.loadingSignal.asReadonly();
  readonly error = this.errorSignal.asReadonly();
  readonly selectedMessage = this.selectedMessageSignal.asReadonly();

  // Computed signals
  readonly messageCount = computed(() => this.messagesSignal().length);
  readonly hasMessages = computed(() => this.messagesSignal().length > 0);
  readonly hasError = computed(() => this.errorSignal() !== null);

  // State update methods
  setMessages(messages: MessageResponse[]): void {
    this.messagesSignal.set(messages);
    this.errorSignal.set(null);
  }

  addMessage(message: MessageResponse): void {
    this.messagesSignal.update(messages => [...messages, message]);
  }

  setLoading(loading: boolean): void {
    this.loadingSignal.set(loading);
  }

  setError(error: string | null): void {
    this.errorSignal.set(error);
    this.loadingSignal.set(false);
  }

  selectMessage(message: MessageResponse | null): void {
    this.selectedMessageSignal.set(message);
  }

  clearMessages(): void {
    this.messagesSignal.set([]);
    this.errorSignal.set(null);
  }

  // Effect example (optional: log state changes in dev)
  constructor() {
    if (!environment.production) {
      effect(() => {
        console.log('Messages state updated:', {
          count: this.messageCount(),
          loading: this.loading(),
          error: this.error()
        });
      });
    }
  }
}
```

---

#### 5.4 Create Message List Component

**Generate component:**
```bash
cd frontend
ng generate component components/message-list --standalone
```

**Update `frontend/src/app/components/message-list/message-list.component.ts`:**
```typescript
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageService } from '../../services/message.service';
import { MessageSignals } from '../../signals/message.signals';

@Component({
  selector: 'app-message-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './message-list.component.html',
  styleUrl: './message-list.component.css'
})
export class MessageListComponent implements OnInit {
  private readonly messageService = inject(MessageService);
  readonly messageSignals = inject(MessageSignals);

  ngOnInit(): void {
    this.loadMessages();
  }

  loadMessages(): void {
    this.messageSignals.setLoading(true);
    this.messageService.getMessages().subscribe({
      next: messages => {
        this.messageSignals.setMessages(messages);
        this.messageSignals.setLoading(false);
      },
      error: error => {
        this.messageSignals.setError(error.message);
      }
    });
  }

  selectMessage(id: number): void {
    const message = this.messageSignals.messages().find(m => m.id === id);
    if (message) {
      this.messageSignals.selectMessage(message);
    }
  }
}
```

**Update `frontend/src/app/components/message-list/message-list.component.html`:**
```html
<div class="container mx-auto px-4 py-8">
  <div class="flex justify-between items-center mb-6">
    <h1 class="text-3xl font-bold text-gray-900">Messages</h1>
    <button
      (click)="loadMessages()"
      class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
      [disabled]="messageSignals.loading()"
    >
      @if (messageSignals.loading()) {
        <span>Loading...</span>
      } @else {
        <span>Refresh</span>
      }
    </button>
  </div>

  @if (messageSignals.hasError()) {
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
      <strong>Error:</strong> {{ messageSignals.error() }}
    </div>
  }

  @if (messageSignals.loading()) {
    <div class="flex justify-center items-center h-64">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
    </div>
  } @else if (messageSignals.hasMessages()) {
    <div class="grid gap-4">
      @for (message of messageSignals.messages(); track message.id) {
        <div
          class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow cursor-pointer"
          (click)="selectMessage(message.id)"
          [class.ring-2]="messageSignals.selectedMessage()?.id === message.id"
          [class.ring-blue-500]="messageSignals.selectedMessage()?.id === message.id"
        >
          <div class="flex justify-between items-start">
            <div class="flex-1">
              <p class="text-gray-800 text-lg">{{ message.content }}</p>
              <p class="text-gray-500 text-sm mt-2">
                Created: {{ message.createdAt | date: 'medium' }}
              </p>
            </div>
            <span class="text-gray-400 font-mono text-sm">#{{ message.id }}</span>
          </div>
        </div>
      }
    </div>
    
    <div class="mt-6 text-center text-gray-600">
      Total messages: {{ messageSignals.messageCount() }}
    </div>
  } @else {
    <div class="text-center py-12">
      <p class="text-gray-500 text-lg">No messages yet. Create your first message!</p>
    </div>
  }
</div>
```

---

#### 5.5 Create Message Form Component

**Generate component:**
```bash
cd frontend
ng generate component components/message-form --standalone
```

**Update `frontend/src/app/components/message-form/message-form.component.ts`:**
```typescript
import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MessageService } from '../../services/message.service';
import { MessageSignals } from '../../signals/message.signals';

@Component({
  selector: 'app-message-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './message-form.component.html',
  styleUrl: './message-form.component.css'
})
export class MessageFormComponent {
  private readonly messageService = inject(MessageService);
  private readonly messageSignals = inject(MessageSignals);

  readonly content = signal<string>('');
  readonly submitting = signal<boolean>(false);

  onSubmit(): void {
    const contentValue = this.content().trim();
    if (!contentValue) return;

    this.submitting.set(true);
    this.messageService.createMessage({ content: contentValue }).subscribe({
      next: message => {
        this.messageSignals.addMessage(message);
        this.content.set('');
        this.submitting.set(false);
      },
      error: error => {
        this.messageSignals.setError(error.message);
        this.submitting.set(false);
      }
    });
  }

  updateContent(value: string): void {
    this.content.set(value);
  }
}
```

**Update `frontend/src/app/components/message-form/message-form.component.html`:**
```html
<div class="bg-white rounded-lg shadow-md p-6">
  <h2 class="text-2xl font-bold text-gray-900 mb-4">Create Message</h2>
  
  <form (ngSubmit)="onSubmit()" class="space-y-4">
    <div>
      <label for="content" class="block text-sm font-medium text-gray-700 mb-2">
        Message Content
      </label>
      <textarea
        id="content"
        [value]="content()"
        (input)="updateContent($any($event.target).value)"
        rows="4"
        class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        placeholder="Enter your message here..."
        [disabled]="submitting()"
        required
      ></textarea>
    </div>

    <div class="flex justify-end">
      <button
        type="submit"
        [disabled]="!content().trim() || submitting()"
        class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
      >
        @if (submitting()) {
          <span>Submitting...</span>
        } @else {
          <span>Create Message</span>
        }
      </button>
    </div>
  </form>
</div>
```

---

#### 5.6 Update App Component and Routes

**Update `frontend/src/app/app.routes.ts`:**
```typescript
import { Routes } from '@angular/router';
import { MessageListComponent } from './components/message-list/message-list.component';

export const routes: Routes = [
  { path: '', redirectTo: '/messages', pathMatch: 'full' },
  { path: 'messages', component: MessageListComponent },
  { path: '**', redirectTo: '/messages' }
];
```

**Update `frontend/src/app/app.component.ts`:**
```typescript
import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MessageFormComponent } from './components/message-form/message-form.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, MessageFormComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'Spring Boot + Angular Template';
}
```

**Update `frontend/src/app/app.component.html`:**
```html
<div class="min-h-screen bg-gray-50">
  <!-- Header -->
  <header class="bg-white shadow-sm">
    <div class="container mx-auto px-4 py-4">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-900">{{ title }}</h1>
        <nav class="flex gap-4">
          <a
            routerLink="/messages"
            routerLinkActive="text-blue-600 font-semibold"
            class="text-gray-600 hover:text-blue-600 transition-colors"
          >
            Messages
          </a>
        </nav>
      </div>
    </div>
  </header>

  <!-- Main Content -->
  <main class="container mx-auto px-4 py-8">
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Form (1/3 width on large screens) -->
      <div class="lg:col-span-1">
        <app-message-form />
      </div>
      
      <!-- Messages List (2/3 width on large screens) -->
      <div class="lg:col-span-2">
        <router-outlet />
      </div>
    </div>
  </main>

  <!-- Footer -->
  <footer class="bg-white border-t border-gray-200 mt-12">
    <div class="container mx-auto px-4 py-6 text-center text-gray-600">
      <p>Spring Boot 3.5.9 + Angular 19 + PostgreSQL + Redis Template</p>
    </div>
  </footer>
</div>
```

---

#### 5.7 Update App Config for HttpClient

**Update `frontend/src/app/app.config.ts`:**
```typescript
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi())
  ]
};
```

**Phase 5 Validation Checklist:**
- [ ] Message list displays data from backend
- [ ] Create message form submits successfully
- [ ] Signals update UI reactively
- [ ] Loading states work correctly
- [ ] Error handling displays properly
- [ ] Tailwind styles render correctly
- [ ] All TypeScript compiles without errors

---

### Phase 6: Documentation & Developer Experience

**Estimated Time:** 1-2 hours

#### 6.1 Update `.gitignore`

**Add to existing `.gitignore`:**
```gitignore
# Node / Angular
node_modules/
frontend/dist/
frontend/.angular/
frontend/coverage/
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
.npm
.yarn
.pnp.*

# Environment files (if you add them later)
.env.local
.env.development.local
.env.test.local
.env.production.local

# IDE - Angular specific
.vscode/launch.json
.vscode/settings.json
```

---

## Summary of Changes

### Files to Create (15+ new files)

1. `.devcontainer/start-servers.sh` - Auto-start script
2. `.vscode/tasks.json` - VS Code tasks
3. `package.json` - Root workspace
4. `.editorconfig` - Code style consistency
5. `frontend/` - Entire Angular application (generated)
6. `frontend/proxy.conf.json` - Dev proxy config
7. `frontend/tailwind.config.js` - Tailwind configuration
8. `frontend/src/environments/*` - Environment files
9. `frontend/src/app/models/message.model.ts` - TypeScript interfaces
10. `frontend/src/app/services/message.service.ts` - HTTP service
11. `frontend/src/app/signals/message.signals.ts` - State management
12. `frontend/src/app/components/message-list/*` - Message list component
13. `frontend/src/app/components/message-form/*` - Message form component
14. `src/main/java/net/profitwarning/api/config/WebConfig.java` - CORS config
15. `src/main/resources/application-openshift.properties` - OpenShift profile

### Files to Modify (4 files)

1. `.devcontainer/Dockerfile` - Add Node.js 22 LTS
2. `.devcontainer/docker-compose.yml` - Add port 4200, node volumes
3. `.devcontainer/devcontainer.json` - Add Angular extensions, update commands
4. `.gitignore` - Add Node/Angular patterns

### Files Unchanged

- `pom.xml` - No changes needed
- `src/main/java/**/*.java` - Controllers already use `/api/v1/**`
- Existing application properties files - Only additions

---

## Effort Estimation

| Phase | Tasks | Estimated Time |
|-------|-------|----------------|
| Phase 1: Devcontainer Infrastructure | 5 tasks | 1-2 hours |
| Phase 2: Angular Application Setup | 6 tasks | 2-3 hours |
| Phase 3: Root Build Coordination | 2 tasks | 30 minutes |
| Phase 4: Backend Integration | 4 tasks | 1 hour |
| Phase 5: Frontend Implementation | 7 tasks | 3-4 hours |
| Phase 6: Documentation | 1 task | 30 minutes |
| **Total** | **25 tasks** | **8-12 hours** |

**For an experienced developer:** 1-2 days
**For someone learning the stack:** 3-5 days

---

## Success Criteria

✅ **Development Environment:**
- [ ] Devcontainer opens without errors
- [ ] Both Node.js 22 and Java 21 available
- [ ] All VS Code extensions installed
- [ ] Ports 4200, 8080, 5432, 6379 accessible

✅ **Angular Application:**
- [ ] `ng serve` runs on port 4200
- [ ] Hot reload works
- [ ] Tailwind CSS styles apply
- [ ] Proxy forwards `/api` requests

✅ **Integration:**
- [ ] Angular calls Spring Boot API successfully
- [ ] CORS allows localhost:4200
- [ ] Data flows from PostgreSQL → Spring Boot → Angular
- [ ] Create/Read operations work end-to-end

✅ **Build & Test:**
- [ ] `npm run build` succeeds
- [ ] `npm test` passes all tests
- [ ] Production builds ready for deployment

---

## Next Steps (After Implementation)

1. **Authentication & Authorization:**
   - Add JWT token service
   - Implement login/logout
   - Secure API endpoints
   - Add auth guards in Angular

2. **Additional Features:**
   - Pagination for message list
   - Search/filter functionality
   - Real-time updates (WebSocket/SSE)
   - File upload support

3. **Production Readiness:**
   - Implement OpenShift deployment (see PLAN_OPENSHIFT_DEPLOYMENT.md)
   - Add monitoring/logging
   - Setup CI/CD pipeline
   - Performance optimization

4. **Code Quality:**
   - Add e2e tests (Playwright/Cypress)
   - Increase test coverage to 80%+
   - Add Husky pre-commit hooks
   - Setup SonarQube analysis

---

## References & Resources

**Angular:**
- [Angular Documentation](https://angular.dev)
- [Angular Signals Guide](https://angular.dev/guide/signals)
- [Tailwind CSS](https://tailwindcss.com)

**Spring Boot:**
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

**DevContainers:**
- [VS Code Dev Containers](https://code.visualstudio.com/docs/devcontainers/containers)
- [IntelliJ Dev Containers](https://www.jetbrains.com/help/idea/connect-to-devcontainer.html)

---

## Support & Troubleshooting

**Common Issues:**

1. **Port conflicts**
   - Kill processes: `lsof -ti:PORT | xargs kill -9`
   - Change ports in docker-compose.yml

2. **Node modules issues**
   - Delete `node_modules` and `package-lock.json`
   - Run `npm install` again

3. **Database connection fails**
   - Check PostgreSQL container: `docker ps`
   - Verify credentials in application-docker.properties

4. **CORS errors**
   - Check `WebConfig.java` allowed origins
   - Verify proxy.conf.json configuration

5. **Build failures**
   - Clear caches: `npm run clean`
   - Rebuild devcontainer: Cmd+Shift+P → "Rebuild Container"

---

**END OF PLAN_ANGULAR_INTEGRATION.md**
