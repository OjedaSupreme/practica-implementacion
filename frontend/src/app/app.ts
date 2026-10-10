import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';

import { ESTADOS, NuevaTarea, PRIORIDADES, Tarea, etiqueta } from './models/tarea';

// No se asusten por el subrayado rojo jejeje

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
  readonly registrando = signal(false);
  readonly errorRegistro = signal('');
  readonly avisoRegistro = signal('');

  nueva: NuevaTarea = this.nuevaVacia();

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

  registrar(formulario: NgForm): void {
    const titulo = this.nueva.titulo.trim();
    const responsable = this.nueva.responsable.trim();
    if (!titulo || !responsable) {
      this.errorRegistro.set('El título y el responsable son obligatorios.');
      return;
    }

    this.registrando.set(true);
    this.errorRegistro.set('');
    this.avisoRegistro.set('');

    const datos: NuevaTarea = { ...this.nueva, titulo, responsable, descripcion: this.nueva.descripcion.trim() };

    this.http.post<Tarea>('/api/tareas', datos).subscribe({
      next: (creada) => {
        this.registrando.set(false);
        this.avisoRegistro.set(`Tarea "${creada.titulo}" registrada.`);
        this.responsables.update((nombres) =>
          [...new Set([...nombres, creada.responsable])].sort((a, b) => a.localeCompare(b))
        );
        this.nueva = this.nuevaVacia();
        formulario.resetForm(this.nueva);
        this.consultar(false);
      },
      error: () => {
        this.registrando.set(false);
        this.errorRegistro.set('No se pudo registrar la tarea. Intenta de nuevo.');
      },
    });
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

  private nuevaVacia(): NuevaTarea {
    return { titulo: '', descripcion: '', estado: 'PENDIENTE', prioridad: 'MEDIA', responsable: '' };
  }

  private tareaBorrador(): Tarea {
    return { id: 0, titulo: '', descripcion: '', estado: 'PENDIENTE', prioridad: 'MEDIA', responsable: '' };
  }
}