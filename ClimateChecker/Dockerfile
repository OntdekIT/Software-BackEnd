# ---- Base JDK ----
FROM eclipse-temurin:21-jdk AS base
ENV APP_PATH app/climatechecker/api-server
EXPOSE 8082
EXPOSE 3306

# ---- Dependencies ----
# Install Maven
FROM base AS dependencies
ENV MAVEN_VERSION="3.9.4"
ENV M2_HOME /opt/maven
ENV PATH=$M2_HOME/bin/:$PATH
RUN cd /opt \
    && wget https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    && tar -zxvf apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    && mv apache-maven-${MAVEN_VERSION} maven

# Copy application source and install Maven dependencies
WORKDIR /${APP_PATH}
COPY . .
RUN mvn clean package

# ---- Flyway Migration Stage ----
FROM flyway/flyway:latest AS flyway

# Copy Flyway configuration
WORKDIR /flyway
COPY ${APP_PATH}/flyway.conf /flyway/conf/flyway.conf

# Copy migrations from the specified directory
COPY ${APP_PATH}/src/main/resources/db.migrations /flyway/sql

# Run migrations
RUN ./flyway migrate

# ---- Release ----
# Copy target .jar file
FROM base AS release
COPY --from=dependencies /${APP_PATH}/target/*.jar climatechecker.jar

# Start the application
ENTRYPOINT ["java", "-jar", "/climatechecker.jar"]
