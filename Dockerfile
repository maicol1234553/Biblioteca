# syntax=docker/dockerfile:1
#
# Dockerfile generico para la RAIZ del repositorio.
#
# Render lo usa cuando un servicio tiene el "Dockerfile Path" por defecto.
# Selecciona el microservicio con el build arg MODULE. Render convierte las
# variables de entorno del servicio en build args, por lo que basta con definir
# MODULE=auth | catalogo | prestamos | notificacion en el servicio.
#
# Los servicios gestionados por el Blueprint (render.yaml) ya incluyen MODULE.

ARG MODULE

FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /build

RUN test -n "$MODULE" \
    || (echo "ERROR: define MODULE=auth|catalogo|prestamos|notificacion en el servicio de Render" >&2 && exit 1)

COPY ${MODULE}/pom.xml ./pom.xml
RUN mvn -B -q -Dmaven.test.skip=true dependency:go-offline

COPY ${MODULE}/src ./src
RUN mvn -B -q -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=70"
USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
