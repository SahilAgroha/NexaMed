# ── Build stage ────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache maven

# Copy pom first — Docker caches this layer.
# Dependencies only re-download when pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Runtime stage (much smaller than JDK) ─────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S nexamed && adduser -S nexamed -G nexamed
USER nexamed

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8761

# UseContainerSupport: respects Docker memory limits (critical in containers)
# MaxRAMPercentage: uses 75% of container RAM, not all of it
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]