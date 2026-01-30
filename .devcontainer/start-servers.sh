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
cd /workspace/backend
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
