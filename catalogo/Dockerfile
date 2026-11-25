# syntax=docker/dockerfile:1
# Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B -q -Dmaven.test.skip=true dependency:go-offline

COPY src ./src
RUN mvn -B -q -Dmaven.test.skip=true package

# Runtime
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
USER app

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:${PORT:-8082}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
