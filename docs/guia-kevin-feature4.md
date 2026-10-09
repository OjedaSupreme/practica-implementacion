# Guía para Kevin — Feature 4: Estados, prioridades y asignación

Rama: `feature/estados-prioridades`  
Repo: `https://github.com/OjedaSupreme/practica-implementacion.git`

---

## Antes de empezar

Necesitas tener instalado:
- Git
- Java 17
- Node.js (para el frontend, aunque no es obligatorio para tu parte)
- VS Code (o IntelliJ, lo que uses)

---

## Paso 1 — Clonar el repo

Abre una terminal y ejecuta esto:

```bash
git clone https://github.com/OjedaSupreme/practica-implementacion.git
cd practica-implementacion
```

---

## Paso 2 — Configurar tu usuario de Git

Esto es importante para que tus commits aparezcan con tu nombre en GitHub.

```bash
git config user.name "Kevin"
git config user.email "tu-correo@utch.edu.mx"
```

Cambia `"Kevin"` y el correo por los tuyos reales (los que usas en GitHub).

---

## Paso 3 — Crear tu rama desde develop

```bash
git checkout develop
git pull origin develop
git checkout -b feature/estados-prioridades
```

Verifica que estás en la rama correcta:

```bash
git branch
```

Debe aparecer `* feature/estados-prioridades`.

---

## Paso 4 — Abrir el proyecto en VS Code

```bash
code .
```

---

## Paso 5 — Editar el backend

### 5.1 — `TareaService.java`

Abre el archivo:

```
backend/src/main/java/com/equipo/sistematareas/service/TareaService.java
```

Al final de la clase, **antes del último `}`**, agrega estos tres métodos:

```java
public Tarea actualizarEstado(Long id, String nuevoEstado) {
    Tarea tarea = tareaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
    tarea.setEstado(EstadoTarea.valueOf(nuevoEstado));
    return tareaRepository.save(tarea);
}

public Tarea actualizarPrioridad(Long id, String nuevaPrioridad) {
    Tarea tarea = tareaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
    tarea.setPrioridad(PrioridadTarea.valueOf(nuevaPrioridad));
    return tareaRepository.save(tarea);
}

public Tarea asignar(Long id, String nuevoResponsable) {
    Tarea tarea = tareaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
    tarea.setResponsable(nuevoResponsable.trim());
    return tareaRepository.save(tarea);
}
```

Agrega también estos imports al inicio del archivo si no están ya:

```java
import com.equipo.sistematareas.model.PrioridadTarea;
```

El archivo completo debe quedar así al final:

```java
package com.equipo.sistematareas.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.equipo.sistematareas.model.EstadoTarea;
import com.equipo.sistematareas.model.PrioridadTarea;
import com.equipo.sistematareas.model.Tarea;
import com.equipo.sistematareas.repository.TareaRepository;

@Service
public class TareaService {

    private final TareaRepository tareaRepository;

    public TareaService(TareaRepository tareaRepository) {
        this.tareaRepository = tareaRepository;
    }

    public List<Tarea> listar(EstadoTarea estado, String responsable) {
        String responsableLimpio = responsable == null ? "" : responsable.trim();
        boolean tieneEstado = estado != null;
        boolean tieneResponsable = !responsableLimpio.isEmpty();

        if (tieneEstado && tieneResponsable) {
            return tareaRepository.findByEstadoAndResponsableIgnoreCaseOrderByIdAsc(estado, responsableLimpio);
        }
        if (tieneEstado) {
            return tareaRepository.findByEstadoOrderByIdAsc(estado);
        }
        if (tieneResponsable) {
            return tareaRepository.findByResponsableIgnoreCaseOrderByIdAsc(responsableLimpio);
        }
        return tareaRepository.findAllByOrderByIdAsc();
    }

    public Tarea actualizar(Long id, Tarea datos) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
        tarea.setTitulo(datos.getTitulo());
        tarea.setDescripcion(datos.getDescripcion());
        tarea.setEstado(datos.getEstado());
        tarea.setPrioridad(datos.getPrioridad());
        tarea.setResponsable(datos.getResponsable());
        return tareaRepository.save(tarea);
    }

    public void eliminar(Long id) {
        if (!tareaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada");
        }
        tareaRepository.deleteById(id);
    }

    public Tarea actualizarEstado(Long id, String nuevoEstado) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
        tarea.setEstado(EstadoTarea.valueOf(nuevoEstado));
        return tareaRepository.save(tarea);
    }

    public Tarea actualizarPrioridad(Long id, String nuevaPrioridad) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
        tarea.setPrioridad(PrioridadTarea.valueOf(nuevaPrioridad));
        return tareaRepository.save(tarea);
    }

    public Tarea asignar(Long id, String nuevoResponsable) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
        tarea.setResponsable(nuevoResponsable.trim());
        return tareaRepository.save(tarea);
    }
}
```

### 5.2 — `TareaController.java`

Abre el archivo:

```
backend/src/main/java/com/equipo/sistematareas/controller/TareaController.java
```

Agrega estos imports al inicio (después de los que ya hay):

```java
import java.util.Map;
import org.springframework.web.bind.annotation.PatchMapping;
```

Luego, **antes del último `}`** de la clase, agrega los tres endpoints:

```java
@PatchMapping("/{id}/estado")
public Tarea actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
    return tareaService.actualizarEstado(id, body.get("estado"));
}

@PatchMapping("/{id}/prioridad")
public Tarea actualizarPrioridad(@PathVariable Long id, @RequestBody Map<String, String> body) {
    return tareaService.actualizarPrioridad(id, body.get("prioridad"));
}

@PatchMapping("/{id}/responsable")
public Tarea asignar(@PathVariable Long id, @RequestBody Map<String, String> body) {
    return tareaService.asignar(id, body.get("responsable"));
}
```

El archivo completo debe quedar así:

```java
package com.equipo.sistematareas.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.equipo.sistematareas.model.EstadoTarea;
import com.equipo.sistematareas.model.Tarea;
import com.equipo.sistematareas.service.TareaService;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @GetMapping
    public List<Tarea> listar(
            @RequestParam(required = false) EstadoTarea estado,
            @RequestParam(required = false) String responsable) {
        return tareaService.listar(estado, responsable);
    }

    @PutMapping("/{id}")
    public Tarea actualizar(@PathVariable Long id, @RequestBody Tarea datos) {
        return tareaService.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tareaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public Tarea actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return tareaService.actualizarEstado(id, body.get("estado"));
    }

    @PatchMapping("/{id}/prioridad")
    public Tarea actualizarPrioridad(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return tareaService.actualizarPrioridad(id, body.get("prioridad"));
    }

    @PatchMapping("/{id}/responsable")
    public Tarea asignar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return tareaService.asignar(id, body.get("responsable"));
    }
}
```

---

## Paso 6 — Primer commit (backend)

```bash
git add backend/src/main/java/com/equipo/sistematareas/service/TareaService.java
git add backend/src/main/java/com/equipo/sistematareas/controller/TareaController.java
git commit -m "feat: agregar endpoints PATCH para estado, prioridad y asignación"
```

---

## Paso 7 — Editar el frontend

### 7.1 — `app.ts`

Abre:

```
frontend/src/app/app.ts
```

Reemplaza todo el contenido con esto:

```typescript
import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ESTADOS, PRIORIDADES, Tarea, etiqueta } from './models/tarea';

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  private readonly http = inject(HttpClient);

  readonly estados = ESTADOS;
  readonly prioridades = PRIORIDADES;
  readonly tareas = signal<Tarea[]>([]);
  readonly responsables = signal<string[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly guardando = signal(false);
  readonly errorGuardar = signal('');

  estado = '';
  responsable = '';

  editando: Tarea | null = null;
  borrador: Tarea = this.tareaBorrador();

  ngOnInit(): void {
    this.consultar(true);
  }

  aplicar(): void {
    this.consultar(false);
  }

  limpiar(): void {
    this.estado = '';
    this.responsable = '';
    this.consultar(false);
  }

  etiquetaDe(valor: string): string {
    return etiqueta(valor);
  }

  abrirEdicion(tarea: Tarea): void {
    this.editando = tarea;
    this.borrador = { ...tarea };
    this.errorGuardar.set('');
  }

  cancelarEdicion(): void {
    this.editando = null;
    this.errorGuardar.set('');
  }

  guardar(): void {
    if (!this.editando) return;
    this.guardando.set(true);
    this.errorGuardar.set('');

    this.http.put<Tarea>(`/api/tareas/${this.borrador.id}`, this.borrador).subscribe({
      next: (actualizada) => {
        this.tareas.update((lista) =>
          lista.map((t) => (t.id === actualizada.id ? actualizada : t))
        );
        this.responsables.set(this.nombresDe(this.tareas()));
        this.editando = null;
        this.guardando.set(false);
      },
      error: () => {
        this.guardando.set(false);
        this.errorGuardar.set('No se pudo guardar la tarea. Intenta de nuevo.');
      },
    });
  }

  eliminar(tarea: Tarea): void {
    if (!confirm(`¿Eliminar la tarea "${tarea.titulo}"?`)) return;

    this.http.delete(`/api/tareas/${tarea.id}`).subscribe({
      next: () => {
        this.tareas.update((lista) => lista.filter((t) => t.id !== tarea.id));
        this.responsables.set(this.nombresDe(this.tareas()));
        if (this.editando?.id === tarea.id) {
          this.editando = null;
        }
      },
      error: () => {
        this.error.set('No se pudo eliminar la tarea. Intenta de nuevo.');
      },
    });
  }

  cambiarEstado(tarea: Tarea, nuevoEstado: string): void {
    this.http.patch<Tarea>(`/api/tareas/${tarea.id}/estado`, { estado: nuevoEstado }).subscribe({
      next: (actualizada) => {
        this.tareas.update((lista) =>
          lista.map((t) => (t.id === actualizada.id ? actualizada : t))
        );
      },
      error: () => {
        this.error.set('No se pudo cambiar el estado.');
      },
    });
  }

  cambiarPrioridad(tarea: Tarea, nuevaPrioridad: string): void {
    this.http.patch<Tarea>(`/api/tareas/${tarea.id}/prioridad`, { prioridad: nuevaPrioridad }).subscribe({
      next: (actualizada) => {
        this.tareas.update((lista) =>
          lista.map((t) => (t.id === actualizada.id ? actualizada : t))
        );
      },
      error: () => {
        this.error.set('No se pudo cambiar la prioridad.');
      },
    });
  }

  reasignar(tarea: Tarea, nuevoResponsable: string): void {
    if (!nuevoResponsable.trim()) return;
    this.http.patch<Tarea>(`/api/tareas/${tarea.id}/responsable`, { responsable: nuevoResponsable }).subscribe({
      next: (actualizada) => {
        this.tareas.update((lista) =>
          lista.map((t) => (t.id === actualizada.id ? actualizada : t))
        );
        this.responsables.set(this.nombresDe(this.tareas()));
      },
      error: () => {
        this.error.set('No se pudo reasignar la tarea.');
      },
    });
  }

  private consultar(guardarResponsables: boolean): void {
    this.cargando.set(true);
    this.error.set('');

    let params = new HttpParams();
    if (this.estado) {
      params = params.set('estado', this.estado);
    }
    if (this.responsable) {
      params = params.set('responsable', this.responsable);
    }

    this.http.get<Tarea[]>('/api/tareas', { params }).subscribe({
      next: (lista) => {
        this.tareas.set(lista);
        if (guardarResponsables) {
          this.responsables.set(this.nombresDe(lista));
        }
        this.cargando.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.error.set('No se pudieron consultar las tareas. Confirma que el backend esté en el puerto 8080.');
      },
    });
  }

  private nombresDe(lista: Tarea[]): string[] {
    return [...new Set(lista.map((tarea) => tarea.responsable))].sort((a, b) => a.localeCompare(b));
  }

  private tareaBorrador(): Tarea {
    return { id: 0, titulo: '', descripcion: '', estado: 'PENDIENTE', prioridad: 'MEDIA', responsable: '' };
  }
}
```

### 7.2 — `app.html`

Abre:

```
frontend/src/app/app.html
```

Reemplaza todo el contenido con esto:

```html
<main>
  <h1>Tareas</h1>

  <form (ngSubmit)="aplicar()">
    <label>
      Estado
      <select name="estado" [(ngModel)]="estado">
        <option value="">Todos</option>
        @for (opcion of estados; track opcion.valor) {
          <option [value]="opcion.valor">{{ opcion.etiqueta }}</option>
        }
      </select>
    </label>

    <label>
      Responsable
      <select name="responsable" [(ngModel)]="responsable">
        <option value="">Todos</option>
        @for (nombre of responsables(); track nombre) {
          <option [value]="nombre">{{ nombre }}</option>
        }
      </select>
    </label>

    <div class="acciones">
      <button type="submit">Aplicar</button>
      <button type="button" class="secundario" (click)="limpiar()">Limpiar</button>
    </div>
  </form>

  @if (error()) {
    <p class="aviso error" role="alert">{{ error() }}</p>
  } @else if (cargando()) {
    <p class="aviso">Consultando tareas...</p>
  } @else if (tareas().length === 0) {
    <p class="aviso">No hay tareas con esos filtros.</p>
  } @else {
    <p class="conteo">{{ tareas().length }} tarea(s)</p>
    <div class="tabla-wrap">
      <table>
        <thead>
          <tr>
            <th>Título</th>
            <th>Descripción</th>
            <th>Estado</th>
            <th>Prioridad</th>
            <th>Responsable</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          @for (tarea of tareas(); track tarea.id) {
            @if (editando?.id === tarea.id) {
              <tr class="fila-edicion">
                <td>
                  <input name="titulo-{{ tarea.id }}" [(ngModel)]="borrador.titulo" required />
                </td>
                <td>
                  <input name="desc-{{ tarea.id }}" [(ngModel)]="borrador.descripcion" />
                </td>
                <td>
                  <select name="estado-{{ tarea.id }}" [(ngModel)]="borrador.estado">
                    @for (opcion of estados; track opcion.valor) {
                      <option [value]="opcion.valor">{{ opcion.etiqueta }}</option>
                    }
                  </select>
                </td>
                <td>
                  <select name="prioridad-{{ tarea.id }}" [(ngModel)]="borrador.prioridad">
                    @for (opcion of prioridades; track opcion.valor) {
                      <option [value]="opcion.valor">{{ opcion.etiqueta }}</option>
                    }
                  </select>
                </td>
                <td>
                  <input name="resp-{{ tarea.id }}" [(ngModel)]="borrador.responsable" required />
                </td>
                <td class="acciones-fila">
                  <button type="button" class="guardar" (click)="guardar()" [disabled]="guardando()">
                    {{ guardando() ? 'Guardando…' : 'Guardar' }}
                  </button>
                  <button type="button" class="secundario" (click)="cancelarEdicion()">Cancelar</button>
                  @if (errorGuardar()) {
                    <p class="aviso error">{{ errorGuardar() }}</p>
                  }
                </td>
              </tr>
            } @else {
              <tr>
                <td>{{ tarea.titulo }}</td>
                <td>{{ tarea.descripcion }}</td>
                <td>
                  <select class="select-rapido" [value]="tarea.estado"
                    (change)="cambiarEstado(tarea, $any($event.target).value)">
                    @for (opcion of estados; track opcion.valor) {
                      <option [value]="opcion.valor">{{ opcion.etiqueta }}</option>
                    }
                  </select>
                </td>
                <td>
                  <select class="select-rapido" [value]="tarea.prioridad"
                    (change)="cambiarPrioridad(tarea, $any($event.target).value)">
                    @for (opcion of prioridades; track opcion.valor) {
                      <option [value]="opcion.valor">{{ opcion.etiqueta }}</option>
                    }
                  </select>
                </td>
                <td>
                  <select class="select-rapido" [value]="tarea.responsable"
                    (change)="reasignar(tarea, $any($event.target).value)">
                    @for (nombre of responsables(); track nombre) {
                      <option [value]="nombre">{{ nombre }}</option>
                    }
                  </select>
                </td>
                <td class="acciones-fila">
                  <button type="button" class="secundario" (click)="abrirEdicion(tarea)">Editar</button>
                  <button type="button" class="eliminar" (click)="eliminar(tarea)">Eliminar</button>
                </td>
              </tr>
            }
          }
        </tbody>
      </table>
    </div>
  }
</main>
```

### 7.3 — `app.css`

Abre:

```
frontend/src/app/app.css
```

Al **final del archivo**, agrega esto:

```css
.select-rapido {
  padding: 0.3rem 0.5rem;
  font: inherit;
  border: 1px solid #cfc5b6;
  border-radius: 6px;
  background: #fffcf8;
  width: 100%;
}
```

---

## Paso 8 — Segundo commit (frontend)

```bash
git add frontend/src/app/app.ts
git add frontend/src/app/app.html
git add frontend/src/app/app.css
git commit -m "feat: agregar selects rapidos para cambiar estado, prioridad y responsable"
```

---

## Paso 9 — Subir tu rama

```bash
git push -u origin feature/estados-prioridades
```

---

## Paso 10 — Abrir el Pull Request en GitHub

1. Entra a `https://github.com/OjedaSupreme/practica-implementacion`
2. Verás un banner amarillo que dice **"Compare & pull request"** para tu rama — haz clic.
3. Asegúrate de que el PR apunte a **`develop`** (no a `main`).
4. Llena el título y descripción con esto:

**Título:**
```
feat: estados, prioridades y asignación rápida de tareas
```

**Descripción:**
```markdown
## Qué cambia
- Tres endpoints PATCH en el backend: `/api/tareas/{id}/estado`, `/api/tareas/{id}/prioridad`, `/api/tareas/{id}/responsable`
- Selects rápidos en cada fila de la tabla para cambiar estado, prioridad y responsable sin abrir el formulario completo de edición

## Cómo probarlo
- Levantar backend (`cd backend && ./mvnw spring-boot:run`) y frontend (`cd frontend && npm start`)
- Abrir `http://localhost:4200`
- En cualquier tarea, cambiar el select de Estado directamente en la tabla — debe actualizarse sin recargar
- Hacer lo mismo con Prioridad y Responsable

## Tarea relacionada
- Funcionalidad asignada: Estados, prioridades y asignación (Feature 4)

## Revisor
- @usuario-de-integrante-5
```

5. Asigna como revisor al **Integrante 5 (Jesus)**.
6. Haz clic en **"Create pull request"**.

---

## Resumen de lo que hiciste

| Archivo | Qué cambió |
|---|---|
| `TareaService.java` | Métodos `actualizarEstado`, `actualizarPrioridad`, `asignar` |
| `TareaController.java` | Endpoints `PATCH /{id}/estado`, `PATCH /{id}/prioridad`, `PATCH /{id}/responsable` |
| `app.ts` | Métodos `cambiarEstado`, `cambiarPrioridad`, `reasignar` |
| `app.html` | Selects rápidos en cada fila de la tabla |
| `app.css` | Estilo `.select-rapido` |

Commits: **2 commits** desde tu cuenta → push → PR a `develop` → revisor: Integrante 5.
