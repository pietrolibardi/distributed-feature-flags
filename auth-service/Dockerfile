# Build from repository root so parent POM and security-lib are available.
# Usage: docker build -f auth-service/Dockerfile .

# -------- BUILD STAGE --------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Root POM and modules (order matters for layer cache)
COPY pom.xml .
COPY security-lib ./security-lib
COPY auth-service ./auth-service

# Build only auth-service and its dependencies (security-lib)
RUN mvn clean package -pl auth-service -am -DskipTests

# -------- RUNTIME STAGE --------
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/auth-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]
