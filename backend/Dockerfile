# syntax=docker/dockerfile:1
# Contenedor del servidor de aplicaciones (API REST Spring Boot).
# Construcción en dos etapas: la primera compila con Maven y la segunda solo lleva el JRE y el .jar.

# ---------- Etapa 1: compilación ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
# Primero el pom.xml: si no cambian las dependencias, Docker reutiliza esta capa en cache.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Las pruebas se ejecutan antes de subir el código (mvn test); aquí solo se empaqueta.
RUN mvn -B -q -DskipTests package && cp target/*.jar /build/app.jar

# ---------- Etapa 2: ejecución ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios: el proceso de Java no corre como root.
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /build/app.jar /app/app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod 755 /app/docker-entrypoint.sh

USER spring

ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"

EXPOSE 8080

# Docker revisa la salud del contenedor con el endpoint de Actuator.
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${PORT}/actuator/health" | grep -q '"UP"' || exit 1

ENTRYPOINT ["/app/docker-entrypoint.sh"]
