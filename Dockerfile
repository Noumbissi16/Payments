# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Leverage Docker cache for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -DskipTests

# Copy the rest of the application code
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/om-api-integration-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 4111
ENTRYPOINT ["java", "-jar", "app.jar"]
