# syntax=docker/dockerfile:1

# Stage 1: Compile and build Spring Boot application
FROM eclipse-temurin:17-jdk AS build

# Set the working directory for the build stage
WORKDIR /workspace/app

# Copy Maven wrapper, project configuration, and source code to the working directory
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Build the Spring Boot application
RUN chmod +x mvnw && ./mvnw clean package -DskipTests


# Stage 2: Run Spring Boot application
FROM eclipse-temurin:17-jre

# Create a temporary volume used by Spring Boot
VOLUME /tmp

# Set the working directory for the runtime stage
WORKDIR /app

# Copy the built Spring Boot jar from the build stage
COPY --from=build /workspace/app/target/*.jar app.jar

# Expose the application port and start the Spring Boot application
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]