# ═══════════════════════════════════════════════════
# ETIA — Facturación Electrónica Perú
# Multi-stage Docker build
# ═══════════════════════════════════════════════════

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (Docker cache layer)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine

# Fuentes para que OpenPDF renderice texto correctamente en Alpine Linux
RUN apk add --no-cache ttf-dejavu fontconfig

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/ms-bs-etia-*.jar app.jar

# Create directories for storage and certs
RUN mkdir -p /app/storage /app/certs

EXPOSE 8080

# JVM tuning for containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

