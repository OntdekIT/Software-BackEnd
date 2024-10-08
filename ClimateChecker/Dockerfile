# ---- Base JDK ----
FROM eclipse-temurin:17-jdk AS base
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
