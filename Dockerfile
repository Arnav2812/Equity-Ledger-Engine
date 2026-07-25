# Base image with JDK 17
FROM eclipse-temurin:17-jdk-alpine

# Set working directory inside container
WORKDIR /app

# Copy built JAR artifact from Maven build target
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose Spring Boot port
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]