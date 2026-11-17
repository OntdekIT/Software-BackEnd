# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Resolve dependencies first so this layer is cached until the pom changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Tests run in CI (see .github/workflows); the image build must not need a database or mail server.
RUN mvn -B -q package -DskipTests && \
    cp target/*.jar /build/app.jar

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre AS release

# Non-root user: a compromised app cannot write to the image or touch the host as root.
RUN groupadd --system app && useradd --system --gid app --no-create-home app

WORKDIR /app
COPY --from=build --chown=app:app /build/app.jar /app/climatechecker.jar

USER app
EXPOSE 8082

# Readiness includes the database; see docs/monitoring.md. start-period leaves room for Flyway migrations.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS http://localhost:8082/actuator/health/readiness || exit 1

# Container-aware heap sizing instead of a hardcoded -Xmx.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["java", "-jar", "/app/climatechecker.jar"]
