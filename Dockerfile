# syntax=docker/dockerfile:1
#
# Dockerfile generico para la RAIZ del repositorio.
#
# Render lo usa cuando un servicio tiene el "Dockerfile Path" por defecto.
# Selecciona el microservicio de dos formas:
#   1. Build arg / variable de entorno MODULE=auth|catalogo|prestamos|notificacion
#   2. Si MODULE no esta definido, deduce el modulo desde RENDER_SERVICE_NAME
#      (por ejemplo "biblioteca-auth-maicol" -> auth).
#
# Los servicios gestionados por el Blueprint (render.yaml) usan directamente
# el Dockerfile de cada modulo, que es la via recomendada.

ARG MODULE
ARG RENDER_SERVICE_NAME

FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE
ARG RENDER_SERVICE_NAME
WORKDIR /build

COPY . .

RUN set -eu; \
    m="${MODULE:-}"; \
    if [ -z "$m" ] && [ -n "${RENDER_SERVICE_NAME:-}" ]; then \
        case "$RENDER_SERVICE_NAME" in \
            *notificacion*) m=notificacion ;; \
            *catalogo*)     m=catalogo ;; \
            *prestamos*)    m=prestamos ;; \
            *auth*)         m=auth ;; \
        esac; \
    fi; \
    test -n "$m" || (echo "ERROR: define MODULE=auth|catalogo|prestamos|notificacion en el servicio de Render" >&2 && exit 1); \
    echo "Compilando microservicio: $m"; \
    mvn -B -q -f "$m/pom.xml" -Dmaven.test.skip=true package; \
    cp "$m"/target/*.jar /build/app.jar

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /build/app.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=70"
USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
