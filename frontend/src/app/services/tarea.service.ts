import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Tarea } from '../models/tarea';

@Injectable({ providedIn: 'root' })
export class TareaService {
  private readonly http = inject(HttpClient);

  listar(estado = '', responsable = ''): Observable<Tarea[]> {
    let params = new HttpParams();
    const estadoLimpio = estado.trim();
    const responsableLimpio = responsable.trim();
    if (estadoLimpio) {
      params = params.set('estado', estadoLimpio);
    }
    if (responsableLimpio) {
      params = params.set('responsable', responsableLimpio);
    }

    return this.http.get<Tarea[]>('/api/tareas', { params });
  }

  consultarPorId(id: number): Observable<Tarea> {
    return this.http.get<Tarea>(`/api/tareas/${id}`);
  }
}
