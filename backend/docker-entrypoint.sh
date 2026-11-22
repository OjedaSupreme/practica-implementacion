#!/bin/sh
# Punto de entrada del contenedor del backend.
# Si el host entrega la base de datos como DATABASE_URL (postgresql://usuario:clave@host[:puerto]/base),
# se convierte al formato JDBC que usa Spring Boot. Si ya vienen las variables SPRING_DATASOURCE_*,
# no se toca nada.
set -e

if [ -n "$DATABASE_URL" ] && [ -z "$SPRING_DATASOURCE_URL" ]; then
  resto="${DATABASE_URL#*://}"      # usuario:clave@host:puerto/base
  credenciales="${resto%%@*}"       # usuario:clave
  servidor="${resto#*@}"            # host:puerto/base
  export SPRING_DATASOURCE_USERNAME="${credenciales%%:*}"
  export SPRING_DATASOURCE_PASSWORD="${credenciales#*:}"
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${servidor}"
  echo "Base de datos: jdbc:postgresql://${servidor%%/*}/... (usuario ${SPRING_DATASOURCE_USERNAME})"
fi

# exec: Java queda como proceso principal y recibe las señales de apagado del contenedor.
exec java $JAVA_OPTS -jar /app/app.jar
