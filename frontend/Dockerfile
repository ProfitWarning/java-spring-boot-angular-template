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
