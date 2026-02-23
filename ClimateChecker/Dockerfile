# ---- Base JDK ----
FROM eclipse-temurin:21-jdk AS base
ENV APP_PATH=app/climatechecker/api-server
EXPOSE 8082

# ---- Dependencies ----
# Use official Maven image with JDK 21
FROM maven:3.9-eclipse-temurin-21 AS dependencies
ENV APP_PATH=app/climatechecker/api-server

# Copy application source and install maven dependencies
WORKDIR /${APP_PATH}
COPY . .
RUN mvn clean package

# ---- Release ----
# Copy target .jar file
FROM base AS release
COPY --from=dependencies /${APP_PATH}/target/*.jar climatechecker.jar

# Start the application
ENTRYPOINT ["java", "-jar", "/climatechecker.jar"]
