# syntax=docker/dockerfile:1
# Contenedor del frontend: compila Angular y lo sirve con Nginx, que además funciona como
# proxy inverso hacia la API (/api) para que navegador, cookie y API compartan el mismo dominio.

# ---------- Etapa 1: compilación de Angular ----------
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npx ng build --configuration production

# ---------- Etapa 2: servidor web Nginx ----------
FROM nginx:stable-alpine

# La imagen oficial de Nginx procesa /etc/nginx/templates/*.template con envsubst al arrancar
# y escribe el resultado en /etc/nginx/conf.d/. Así el puerto y la URL de la API se configuran
# con variables de entorno, sin reconstruir la imagen.
RUN rm -f /etc/nginx/conf.d/default.conf
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# PORT: puerto donde escucha Nginx (Render lo cambia a 10000).
# API_URL: dirección del contenedor del backend.
# NGINX_ENTRYPOINT_LOCAL_RESOLVERS: expone el DNS del contenedor como ${NGINX_LOCAL_RESOLVERS}.
ENV PORT=80 \
    API_URL=http://backend:8080 \
    NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${PORT}/" > /dev/null || exit 1
