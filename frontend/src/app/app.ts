import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ESTADOS, Tarea, etiqueta } from './models/tarea';

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  private readonly http = inject(HttpClient);

  readonly estados = ESTADOS;
  readonly tareas = signal<Tarea[]>([]);
  readonly responsables = signal<string[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');

  estado = '';
  responsable = '';

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
}
