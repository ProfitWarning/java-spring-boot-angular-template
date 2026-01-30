# Build stage using Maven
FROM maven:3.9-eclipse-temurin-25-alpine AS builder
WORKDIR /workspace
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src src
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Create a non-root user for OpenShift compatibility
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the jar from build stage
COPY --from=builder /workspace/target/api-service.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
