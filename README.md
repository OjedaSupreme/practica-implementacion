# Sistema de gestión de tareas

Aplicación pequeña para registrar, consultar, modificar, eliminar y filtrar tareas de un equipo. Cada tarea tiene título, descripción, estado (Pendiente, En proceso, Terminada), prioridad (Baja, Media, Alta) y un responsable.

Este repositorio es la evidencia principal del proceso de control de versiones. Cada integrante trabaja en su propia rama, con commits, push, Pull Request, revisión e integración a `main`.


## Integrantes

| Integrante | Actividad | Rama | Revisa el PR de |
| --- | --- | --- | --- |
| 1 Daniel | Registro de tareas | `feature/registro-tareas` | Integrante 2 |
| 2 Derek | Consulta de tareas | `feature/consulta-tareas` | Integrante 3 |
| 3 Susana | Modificar y eliminar tareas | `feature/modificar-eliminar-tareas` | Integrante 4 |
| 4 Kevin | Estados, prioridades y asignación | `feature/estados-prioridades` | Integrante 5 |
| 5 Jesus | Filtros por estado y responsable | `feature/filtros` | Integrante 1 |

## Tecnologías

- Frontend: Angular
- Backend: Spring Boot (API REST)
- Java 21
- Node.js 20.19 o superior (también sirve Node.js 22)
- Base de datos: H2 en memoria (desarrollo) y PostgreSQL 16 (contenedores y producción)
- Contenedores: Docker, Docker Compose y Render
- Plataforma: GitHub
- Flujo: GitHub Flow

## Cómo ejecutarlo

Hacen falta Java 21, Node.js y dos terminales. La primera vez Maven y npm descargan dependencias, así que hace falta internet.

Backend, en el puerto 8080:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Frontend, en el puerto 4200. `npm install` solo hace falta la primera vez:

```powershell
cd frontend
npm install
npm start
```

Abre `http://localhost:4200`. Ahí se ve la lista de tareas y los filtros.

La consola de H2 queda en `http://localhost:8080/h2-console`. JDBC URL: `jdbc:h2:mem:tareasdb`. Usuario: `sa`. Contraseña vacía.

## API

- `GET /api/tareas` devuelve una lista ordenada por identificador ascendente. Acepta los filtros opcionales `estado` y `responsable`; sin coincidencias responde `200 OK` con `[]`.
- `GET /api/tareas?estado=PENDIENTE` filtra por estado. Valores: `PENDIENTE`, `EN_PROCESO`, `TERMINADA`.
- `GET /api/tareas?responsable=Nombre` filtra por responsable.
- Los dos parámetros se pueden usar juntos.
- `GET /api/tareas/{id}` devuelve una tarea por su identificador con `200 OK`. Si no existe, responde `404 Not Found` con el mensaje `Tarea no encontrada`.

  La respuesta de ambos endpoints de consulta usa los campos `id` (número), `titulo` (texto), `descripcion` (texto), `estado` (`PENDIENTE`, `EN_PROCESO` o `TERMINADA`), `prioridad` (`BAJA`, `MEDIA` o `ALTA`) y `responsable` (texto). Ejemplo:

  ```json
  {
    "id": 1,
    "titulo": "Preparar reporte",
    "descripcion": "Capturas del PR",
    "estado": "PENDIENTE",
    "prioridad": "ALTA",
    "responsable": "Daniel"
  }
  ```

- `POST /api/tareas` registra una tarea y responde `201 Created`. `titulo` y `responsable` son obligatorios; si no se envían `estado` o `prioridad`, quedan en `PENDIENTE` y `MEDIA`. Si faltan datos obligatorios o un valor no es válido, responde `400`.

  ```json
  { "titulo": "Preparar reporte", "descripcion": "Capturas del PR", "estado": "PENDIENTE", "prioridad": "ALTA", "responsable": "Daniel" }
  ```

- `PUT /api/tareas/{id}` reemplaza título, descripción, estado, prioridad y responsable. Responde `200` con la tarea actualizada, o `404` si el id no existe.
- `DELETE /api/tareas/{id}` elimina la tarea y responde `204`. Si no existe, responde `404`.
- `PATCH /api/tareas/{id}/estado` con `{ "estado": "TERMINADA" }` cambia solo el estado.
- `PATCH /api/tareas/{id}/prioridad` con `{ "prioridad": "ALTA" }` cambia solo la prioridad.
- `PATCH /api/tareas/{id}/responsable` con `{ "responsable": "Ana" }` cambia solo el responsable. Los espacios al inicio y al final se ignoran.

## Autenticación y autorización (JWT)

La API está protegida con **Spring Security** y **JSON Web Tokens**:

- `POST /api/auth/login` con `{ "username": "...", "password": "..." }` devuelve un *access token* JWT (15 min) y deja el *refresh token* (7 días) en una cookie `HttpOnly`, `SameSite=Strict`, con `Path=/api/auth`.
- Toda ruta `/api/tareas/**` exige `Authorization: Bearer <accessToken>`. `DELETE` solo lo puede hacer el rol `ADMIN`.
- `POST /api/auth/refresh` usa la cookie para entregar un access token nuevo y **rota** el refresh token (el anterior queda revocado; si alguien lo reutiliza, se cierran todas las sesiones de ese usuario).
- `POST /api/auth/logout` revoca el refresh token y borra la cookie.
- `GET /api/auth/me` devuelve el usuario y los roles del token.

Errores en JSON: `401` con `codigo` = `SIN_CREDENCIALES`, `TOKEN_INVALIDO`, `TOKEN_EXPIRADO` o `CREDENCIALES_INVALIDAS`; `403` con `ACCESO_DENEGADO`.

Usuarios de prueba (se crean al arrancar, solo desarrollo):

| Usuario | Contraseña | Rol |
| --- | --- | --- |
| `admin` | `Admin123*` | ADMIN |
| `jesus` | `Usuario123*` | USUARIO |

La clave de firma se toma de la variable de entorno `JWT_SECRET` (Base64, mínimo 256 bits); el valor de `application.properties` es solo para desarrollo. En producción también hay que poner `jwt.cookie-segura=true` (HTTPS).

Pruebas de seguridad:

- Automáticas: `.\mvnw.cmd "-Dtest=SeguridadAutenticacionTest" test`
- Postman: importar `docs/autenticacion.postman_collection.json` y ejecutar la colección en orden.
- PowerShell 7: `.\docs\pruebas-seguridad.ps1` con el backend levantado.

En el frontend, `authInterceptor` agrega el encabezado `Bearer` y, ante un `401`, renueva el token y repite la petición; `authGuard` manda a `/login` si no hay sesión. El access token vive solo en memoria.

## Despliegue con contenedores

La aplicación se ejecuta en tres contenedores:

| Contenedor | Imagen | Función |
| --- | --- | --- |
| `frontend` | `frontend/Dockerfile` (Node 22 → Nginx) | Sirve Angular y reenvía `/api` al backend (proxy inverso). |
| `backend` | `backend/Dockerfile` (Maven → JRE 21) | API REST Spring Boot con el perfil `prod`. |
| `db` | `postgres:16-alpine` | Servidor de base de datos con volumen persistente. |

### Local (Docker Desktop)

```powershell
docker compose up -d --build
docker compose ps
```

Abre `http://localhost:8081`. Para detener: `docker compose down` (agrega `-v` para borrar también los datos).

### Nube (Render)

`render.yaml` es un *Blueprint*: crea la base PostgreSQL administrada y los dos servicios web con Docker. Render publica cada servicio en un subdominio `onrender.com` con certificado TLS administrado y renovación automática.

Variables de entorno:

| Variable | Servicio | Valor |
| --- | --- | --- |
| `DATABASE_URL` | API | Cadena interna de PostgreSQL (la convierte `docker-entrypoint.sh`). |
| `JWT_SECRET` | API | Generada por Render. |
| `CORS_ORIGINS` | API | URL del frontend, p. ej. `https://tareas-ojeda-web.onrender.com`. |
| `APP_ADMIN_PASSWORD`, `APP_USUARIO_PASSWORD` | API | Contraseñas iniciales de `admin` y `jesus`. |
| `API_URL` | Web | URL de la API, p. ej. `https://tareas-ojeda-api.onrender.com`. |

## Guía de pruebas de funcionamiento

La base H2 está en memoria y arranca vacía. Cada reinicio del backend borra las tareas. Primero hay que registrar alguna.

### Pruebas automáticas

Desde `backend`:

```powershell
cd backend
.\mvnw.cmd test
```

El resultado esperado es `BUILD SUCCESS`. Cubren el registro, la consulta por id, los filtros, la modificación, la eliminación y el cambio de estado, prioridad y responsable.

Para repetir solo una clase:

```powershell
.\mvnw.cmd "-Dtest=FiltroTareasTest" test
.\mvnw.cmd "-Dtest=OperacionesTareaTest" test
.\mvnw.cmd "-Dtest=TareaControllerTest" test
```

En PowerShell las comillas alrededor de `-Dtest=...` son necesarias.

### Pruebas por la API

Con el backend en el puerto 8080, en otra terminal de PowerShell. Primero se obtiene un token y se envía en cada petición:

```powershell
$base = "http://localhost:8080/api/tareas"
$token = (Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" -ContentType "application/json" -Body '{"username":"admin","password":"Admin123*"}').accessToken
$PSDefaultParameterValues["Invoke-RestMethod:Headers"] = @{ Authorization = "Bearer $token" }

$creada = Invoke-RestMethod -Method Post -Uri $base -ContentType "application/json" -Body '{"titulo":"Preparar reporte","descripcion":"Prueba manual","estado":"PENDIENTE","prioridad":"ALTA","responsable":"Ana"}'
$id = $creada.id

Invoke-RestMethod -Method Post -Uri $base -ContentType "application/json" -Body '{"titulo":"Revisar PR","responsable":"Luis","estado":"EN_PROCESO","prioridad":"MEDIA"}'
Invoke-RestMethod "${base}/${id}"
Invoke-RestMethod "${base}?estado=PENDIENTE"
Invoke-RestMethod "${base}?responsable=ana"
Invoke-RestMethod "${base}?estado=PENDIENTE&responsable=Luis"

Invoke-RestMethod -Method Put -Uri "${base}/${id}" -ContentType "application/json" -Body '{"titulo":"Reporte listo","descripcion":"Editada","estado":"EN_PROCESO","prioridad":"BAJA","responsable":"Ana"}'
Invoke-RestMethod -Method Patch -Uri "${base}/${id}/estado" -ContentType "application/json" -Body '{"estado":"TERMINADA"}'
Invoke-RestMethod -Method Patch -Uri "${base}/${id}/prioridad" -ContentType "application/json" -Body '{"prioridad":"ALTA"}'
Invoke-RestMethod -Method Patch -Uri "${base}/${id}/responsable" -ContentType "application/json" -Body '{"responsable":"Marta"}'
Invoke-RestMethod -Method Delete -Uri "${base}/${id}"
```

Qué debe ocurrir:

| Paso | Resultado |
| --- | --- |
| `POST` con título y responsable | `201` y un `id` nuevo. Sin estado ni prioridad quedan `PENDIENTE` y `MEDIA`. |
| `POST` sin título o sin responsable | `400` y la tarea no se guarda. |
| `GET /api/tareas/{id}` de un id que existe | `200` con esa tarea. |
| `GET /api/tareas/99999` | `404`. |
| `GET` con `estado=PENDIENTE` | Solo tareas pendientes. `URGENTE` responde `400`. |
| `GET` con `responsable=ana` o `  Ana  ` | Las tareas de Ana, sin distinguir mayúsculas. |
| `GET` con estado y responsable que no coinciden | `200` y una lista vacía. |
| `PUT` | `200` con los campos nuevos. Un id inexistente responde `404`. |
| `PATCH` de estado, prioridad o responsable | `200` y solo cambia ese campo. |
| `DELETE` | `204`. Un segundo delete del mismo id responde `404`. |

### Pruebas en la pantalla

Con backend en 8080 y frontend en 4200, abre `http://localhost:4200`.

1. **Registro.** En «Nueva tarea» escribe título y responsable, pulsa Registrar. Debe aparecer el aviso de tarea registrada y una fila en la tabla. Si falta el título o el responsable, el botón queda deshabilitado.
2. **Consulta por id.** Escribe el id de esa fila y pulsa Consultar. El aviso muestra título, estado, prioridad y responsable. Un id que no existe dice que no hay tarea con ese identificador.
3. **Filtros.** Elige Pendiente y pulsa Aplicar: el conteo dice «con estado Pendiente» y solo quedan esas filas. Elige un responsable y aplica de nuevo. Limpiar vuelve a mostrar todas. Una combinación sin resultados dice «No hay tareas con estado … y responsable …». Sin tareas y sin filtro, el aviso es «No hay tareas registradas».
4. **Modificar.** Pulsa Editar, cambia el título y pulsa Guardar. La fila muestra el título nuevo. Cancelar descarta el cambio.
5. **Eliminar.** Pulsa Eliminar y confirma. La fila desaparece.
6. **Estado, prioridad y responsable.** En la fila, cambia cada selector. La tarea se actualiza sin abrir el formulario de edición.

## Flujo de trabajo.

- `main` es la rama estable. Nadie hace push directo.
- Las ramas salen de `main` actualizada: `git pull` y después `git checkout -b feature/filtros`.
- Una rama, una responsabilidad.
- Commits pequeños, desde la cuenta de cada quien. Mínimo 3 por integrante.
- Mensaje con prefijo: `feat:`, `fix:`, `docs:`, `refactor:`.
- El Pull Request apunta a `main`, con revisor asignado. Nadie aprueba el propio.
- La integración usa **Create a merge commit**. El autor combina después de la aprobación.
- La rama se elimina al integrarse.

## Estructura

```text
backend/     Spring Boot: model, repository, service, controller
frontend/    Angular: componentes y servicios
docs/        Capturas y material del reporte
```

## Plantilla de Pull Request del integrante 5

Título: `feat: filtros por estado y responsable`

```markdown
## Qué cambia
- Consulta de tareas por estado, por responsable o por ambos.
- Pantalla de filtros en Angular.

## Cómo probarlo
- Levantar backend (8080) y frontend (4200).
- Abrir http://localhost:4200, elegir Pendiente y comprobar que solo salen tareas pendientes.
- Elegir un responsable y comprobar que solo salen sus tareas.

## Tarea relacionada
- Funcionalidad asignada: Filtros por estado y responsable

## Revisor
- @usuario-del-integrante-1
```
