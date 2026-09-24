# Stage 1: Build application with Maven
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image (< 200MB)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: run under unprivileged user
RUN addgroup -S authguard && adduser -S authguard -G authguard
USER authguard

COPY --from=builder /build/target/authguard-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Tuned JVM parameters for 1GB RAM VPS execution
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
