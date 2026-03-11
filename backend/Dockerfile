# ── Stage 1: Build ─────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy Maven config files first (cached layer for dependencies)
COPY pom.xml settings.xml ./

# Download all dependencies into the cache layer
RUN mvn -s settings.xml dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn -s settings.xml package -q -DskipTests

# ── Stage 2: Runtime ───────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
