# Use a Java 21 runtime as a base image
FROM eclipse-temurin:21-jre

# Set the working directory
WORKDIR /app

# Copy the jar file into the container
COPY target/crypto-recommendation-service-0.0.1-SNAPSHOT.jar /app/recommendation-service.jar

# Expose port 8080
EXPOSE 8080

# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "recommendation-service.jar"]
