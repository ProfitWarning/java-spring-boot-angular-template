# Frontend - Angular Application

Angular 21 standalone application with Tailwind CSS v4 and Signal-based state management.

## 🏗️ Architecture

```
src/
├── app/
│   ├── components/      # UI components
│   │   ├── message-list/
│   │   └── message-form/
│   ├── services/        # HTTP services
│   │   └── message.service.ts
│   ├── signals/         # State management
│   │   └── message.signals.ts
│   ├── models/          # TypeScript interfaces
│   │   └── message.model.ts
│   ├── app.component.*  # Root component
│   ├── app.config.ts    # Application configuration
│   └── app.routes.ts    # Route configuration
├── environments/        # Environment-specific configs
│   ├── environment.ts
│   ├── environment.development.ts
│   └── environment.production.ts
├── assets/             # Static assets
└── styles.css          # Global styles + Tailwind
```

## 🚀 Running Locally

### From Root Directory (Recommended)

```bash
# Start frontend only
npm run frontend

# Start both frontend and backend
npm run dev
```

### From Frontend Directory

```bash
cd frontend
npm start
```

### Access Application

- **Development Server:** http://localhost:4200
- **Auto-reloads** on file changes

### Build for Development

```bash
cd frontend
npm run build
```

Output: `dist/` directory

## 🧪 Testing

### Run Tests (Watch Mode)

```bash
# From root
npm run test:frontend

# From frontend directory
cd frontend
npm test
```

### Run Tests (CI Mode)

```bash
cd frontend
npm run test:ci
```

### Run Tests with Coverage

```bash
cd frontend
npm run test:coverage
```

Coverage report: `coverage/` directory

### Run Specific Test File

```bash
cd frontend
npm test -- --include='**/message.service.spec.ts'
```

## 📦 Building

### Development Build

```bash
# From root
npm run build:frontend

# From frontend directory
cd frontend
npm run build
```

### Production Build

```bash
cd frontend
npm run build:prod
```

**Optimizations:**
- Ahead-of-Time (AOT) compilation
- Tree shaking
- Minification
- Source maps (optional)

Output: `dist/frontend/browser/`

### Build with Specific Environment

```bash
cd frontend
ng build --configuration=production
```

## 🎨 Styling

### Tailwind CSS v4

**Configuration:** `tailwind.config.js`

```javascript
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

### Global Styles

**File:** `src/styles.css`

```css
@import "tailwindcss";

/* Tailwind directives */
@layer base { ... }
@layer components { ... }
@layer utilities { ... }
```

### Component Styles

Components can use:
- **Tailwind utility classes** (recommended)
- **Component-scoped CSS** (`.component.css`)
- **Inline styles** (when necessary)

### Customization

Add custom utilities in `src/styles.css`:

```css
@layer utilities {
  .custom-class {
    /* your styles */
  }
}
```

## 🔌 API Integration

### Proxy Configuration

**File:** `proxy.conf.json`

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true
  }
}
```

**Purpose:** Routes `/api/**` requests to backend during development

### Environment Configuration

#### Development (`environment.development.ts`)

```typescript
export const environment = {
  production: false,
  apiUrl: '' // Uses proxy
};
```

#### Production (`environment.production.ts`)

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://your-backend-url'
};
```

### HTTP Service Example

```typescript
constructor(private http: HttpClient) {}

getAllMessages(): Observable<MessageResponse[]> {
  return this.http.get<MessageResponse[]>(`${environment.apiUrl}/api/v1/messages`);
}
```

## 🗂️ State Management

### Angular Signals

**File:** `src/app/signals/message.signals.ts`

#### Writable Signals

```typescript
export const messages = signal<Message[]>([]);
export const loading = signal<boolean>(false);
export const error = signal<string | null>(null);
```

#### Computed Signals

```typescript
export const hasMessages = computed(() => messages().length > 0);
export const messageCount = computed(() => messages().length);
export const sortedMessages = computed(() => 
  [...messages()].sort((a, b) => 
    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  )
);
```

#### Usage in Components

```typescript
export class MessageListComponent {
  messages = messages;
  loading = loading;
  error = error;
  hasMessages = hasMessages;
}
```

**Template:**
```html
@if (loading()) {
  <p>Loading...</p>
} @else if (error()) {
  <p>Error: {{ error() }}</p>
} @else if (hasMessages()) {
  <div>{{ messageCount() }} messages</div>
}
```

### Why Signals Instead of NgRx?

- **Simpler:** Less boilerplate, easier to understand
- **Performant:** Fine-grained reactivity
- **Native:** Built into Angular 21
- **Type-safe:** Full TypeScript support
- **Composable:** Easy to combine with `computed()`

## 📡 Services

### MessageService

**File:** `src/app/services/message.service.ts`

#### Methods

| Method | Description | Return Type |
|--------|-------------|-------------|
| `getAllMessages()` | Fetch all messages | `Observable<MessageResponse[]>` |
| `getMessageById(id)` | Fetch single message | `Observable<MessageResponse>` |
| `createMessage(command)` | Create new message | `Observable<MessageResponse>` |

#### Features

- **Automatic Retry:** 3 attempts with exponential backoff
- **Error Handling:** Catches and transforms HTTP errors
- **Type Safety:** Strongly typed DTOs
- **Environment-aware:** Uses `environment.apiUrl`

#### Example Usage

```typescript
constructor(private messageService: MessageService) {}

ngOnInit(): void {
  this.messageService.getAllMessages()
    .subscribe({
      next: (data) => messages.set(data),
      error: (err) => error.set(err.message),
      complete: () => loading.set(false)
    });
}
```

## 🧩 Components

### MessageListComponent

**Location:** `src/app/components/message-list/`

**Purpose:** Display all messages in card format

**Features:**
- Loading state indicator
- Error message display
- Empty state message
- Responsive Tailwind styling
- Sorted by creation date (newest first)

**Template Highlights:**
```html
@if (loading()) {
  <div class="loading">Loading messages...</div>
} @else if (error()) {
  <div class="error">{{ error() }}</div>
} @else if (hasMessages()) {
  @for (message of sortedMessages(); track message.id) {
    <div class="message-card">...</div>
  }
} @else {
  <p class="empty-state">No messages yet</p>
}
```

### MessageFormComponent

**Location:** `src/app/components/message-form/`

**Purpose:** Create new messages

**Features:**
- Form validation (required, min/max length)
- Success/error feedback
- Auto-clear on success
- Tailwind-styled form
- Reactive forms

**Form Controls:**
```typescript
messageForm = new FormGroup({
  content: new FormControl('', [
    Validators.required,
    Validators.minLength(1),
    Validators.maxLength(255)
  ])
});
```

## 🛠️ Code Quality

### Linting

```bash
# From root
npm run lint

# From frontend directory
cd frontend
npm run lint
```

**Linter:** ESLint (pending setup)

### Formatting

```bash
# From root
npm run format

# From frontend directory
cd frontend
npm run format
```

**Formatter:** Prettier (pending setup)

### Type Checking

```bash
cd frontend
npm run type-check
```

## 📝 Available Scripts

| Command | Description |
|---------|-------------|
| `npm start` | Start development server |
| `npm run build` | Development build |
| `npm run build:prod` | Production build |
| `npm test` | Run tests (watch mode) |
| `npm run test:ci` | Run tests (CI mode) |
| `npm run test:coverage` | Run tests with coverage |
| `npm run lint` | Lint TypeScript/HTML |
| `npm run format` | Format code with Prettier |
| `npm run type-check` | TypeScript type checking |

## 🔧 Configuration Files

### angular.json

Main Angular CLI configuration:
- Build configurations
- Development server settings
- File replacement rules
- Proxy configuration
- Asset management

**Key Settings:**
```json
{
  "serve": {
    "options": {
      "host": "0.0.0.0",  // Devcontainer access
      "proxyConfig": "proxy.conf.json"
    }
  }
}
```

### tsconfig.json

TypeScript compiler configuration:
- Strict type checking enabled
- ES2022 target
- Module resolution settings
- Path mappings

### tailwind.config.js

Tailwind CSS configuration:
- Content paths for purging
- Theme customization
- Plugin configuration

### proxy.conf.json

Development proxy for backend API:
- Routes `/api/**` to `http://localhost:8080`
- CORS handling during development

## 📚 Key Dependencies

### Core

| Dependency | Version | Purpose |
|------------|---------|---------|
| `@angular/core` | 21.x | Angular framework |
| `@angular/common` | 21.x | Common directives/pipes |
| `@angular/router` | 21.x | Routing |
| `@angular/forms` | 21.x | Reactive forms |

### HTTP

| Dependency | Purpose |
|------------|---------|
| `@angular/common/http` | HTTP client |

### Styling

| Dependency | Version | Purpose |
|------------|---------|---------|
| `tailwindcss` | 4.1.18 | Utility-first CSS |

### Development

| Dependency | Purpose |
|------------|---------|
| `typescript` | 5.7 |
| `@angular-devkit/build-angular` | Build tooling |
| `@angular/cli` | Angular CLI |

### Testing

| Dependency | Purpose |
|------------|---------|
| `jasmine-core` | Test framework |
| `karma` | Test runner |
| `@angular/platform-browser-dynamic` | Testing utilities |

## 🎯 Features

### Current Features

- ✅ **Standalone Components** - No NgModule required
- ✅ **Signal-based State** - Modern reactive state management
- ✅ **TypeScript Strict Mode** - Full type safety
- ✅ **Reactive Forms** - Form validation and handling
- ✅ **HTTP Interceptors** - Request/response handling
- ✅ **Error Handling** - Graceful error management
- ✅ **Loading States** - User feedback during operations
- ✅ **Responsive Design** - Tailwind CSS utilities
- ✅ **Proxy Configuration** - Seamless API integration
- ✅ **Environment Configuration** - Dev/prod settings

### Planned Features

- ⏳ **ESLint Configuration** - Code quality enforcement
- ⏳ **Prettier Setup** - Automatic code formatting
- ⏳ **Pre-commit Hooks** - Husky integration
- ⏳ **E2E Testing** - Playwright or Cypress
- ⏳ **Authentication** - JWT token handling
- ⏳ **Route Guards** - Protected routes
- ⏳ **Lazy Loading** - Route-based code splitting
- ⏳ **PWA Support** - Service workers
- ⏳ **Internationalization** - i18n support

## 🔍 Debugging

### Browser DevTools

1. **Open DevTools:** F12 or Ctrl+Shift+I
2. **Angular DevTools Extension:** Install for component inspection
3. **Source Maps:** Enabled in development for debugging

### VS Code Debugging

**Configuration:** `.vscode/launch.json`

```json
{
  "type": "chrome",
  "request": "launch",
  "name": "Angular",
  "url": "http://localhost:4200",
  "webRoot": "${workspaceFolder}/frontend"
}
```

### Common Issues

#### Port 4200 already in use

```bash
# Kill process on port 4200 (Windows)
netstat -ano | findstr :4200
taskkill /PID <PID> /F
```

#### Module not found errors

```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

#### Tailwind classes not working

```bash
cd frontend
rm -rf .angular
npm start
```

## 📊 Performance

### Build Optimization

- **Production Build:** Automatically minifies and optimizes
- **Lazy Loading:** Load routes on-demand (planned)
- **Tree Shaking:** Remove unused code
- **AOT Compilation:** Faster runtime performance

### Runtime Performance

- **OnPush Change Detection:** Use where appropriate
- **Track By Functions:** In `@for` loops for better rendering
- **Computed Signals:** Memoized derived state
- **HTTP Caching:** Leverage browser cache

### Bundle Analysis

```bash
cd frontend
npm run build:prod -- --stats-json
npx webpack-bundle-analyzer dist/frontend/browser/stats.json
```

## 📝 Development Workflow

1. **Create Feature Branch:**
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Start Dev Server:**
   ```bash
   npm run frontend
   ```

3. **Make Changes:** Edit components, services, etc.

4. **Test Changes:**
   ```bash
   npm run test:frontend
   ```

5. **Lint and Format:**
   ```bash
   npm run lint:frontend
   npm run format
   ```

6. **Build for Production:**
   ```bash
   npm run build:frontend
   ```

7. **Commit and Push:**
   ```bash
   git add .
   git commit -m "feat(frontend): add my feature"
   git push origin feature/my-feature
   ```

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

## 🔗 Related Documentation

- **[Root README](../README.md)** - Monorepo overview
- **[Backend README](../backend/README.md)** - Spring Boot API

## 📖 Resources

### Official Documentation

- **Angular:** https://angular.dev
- **Tailwind CSS:** https://tailwindcss.com
- **TypeScript:** https://www.typescriptlang.org
- **RxJS:** https://rxjs.dev

### Tutorials

- Angular Signals: https://angular.dev/guide/signals
- Standalone Components: https://angular.dev/guide/components
- Reactive Forms: https://angular.dev/guide/forms

---

**Frontend Status:** Active Development  
**Angular Version:** 21  
**Node Version:** 24 LTS  
**Last Updated:** January 30, 2026
