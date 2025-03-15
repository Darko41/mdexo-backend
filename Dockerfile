# Step 1: Use the official OpenJDK 21 image to build the Java application
FROM openjdk:21-jdk-slim as build

# Step 2: Set the working directory inside the container for the build stage
WORKDIR /app

# Step 3: Copy the Maven wrapper (if using it) and POM file (if using Maven) into the container
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Step 4: Download dependencies (this will cache dependencies unless the pom.xml changes)
RUN ./mvnw dependency:go-offline

# Step 5: Copy the rest of your project files into the container
COPY src ./src

# Step 6: Package the Spring Boot application (create the JAR file)
RUN ./mvnw clean package -DskipTests

# Step 7: Use a smaller image with only the JAR file and JRE (Java Runtime Environment)
FROM openjdk:21-jdk-slim

# Set the working directory for the runtime container
WORKDIR /app

# Copy the JAR file from the build stage into the runtime image
COPY --from=build /app/target/mdexo-backend-0.0.1-SNAPSHOT.jar /app/mdexo-backend-0.0.1-SNAPSHOT.jar

# Step 8: Expose the port your application will run on (default is 8080 for Spring Boot)
EXPOSE 8080

# Step 9: Define the entry point for your Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/mdexo-backend-0.0.1-SNAPSHOT.jar"]
