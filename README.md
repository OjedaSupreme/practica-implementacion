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
- Java 17
- Node.js
- Base de datos: H2 en memoria
- Plataforma: GitHub
- Flujo: GitHub Flow

## Cómo ejecutarlo

Hacen falta dos terminales.

Backend, en el puerto 8080:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Frontend, en el puerto 4200:

```powershell
cd frontend
npm start
```

Abre `http://localhost:4200`. Ahí se ve la lista de tareas y los filtros.

La consola de H2 queda en `http://localhost:8080/h2-console`. JDBC URL: `jdbc:h2:mem:tareasdb`. Usuario: `sa`. Contraseña vacía.

## API

- `GET /api/tareas` devuelve todas las tareas.
- `GET /api/tareas?estado=PENDIENTE` filtra por estado. Valores: `PENDIENTE`, `EN_PROCESO`, `TERMINADA`.
- `GET /api/tareas?responsable=Nombre` filtra por responsable.
- Los dos parámetros se pueden usar juntos.

## Flujo de trabajo

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
