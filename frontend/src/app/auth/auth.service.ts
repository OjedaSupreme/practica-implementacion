import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, finalize, map, of, shareReplay, tap } from 'rxjs';

import { Sesion, TokenResponse } from './auth.models';

/**
 * Maneja la sesión del usuario.
 *
 * - El access token (JWT) vive SOLO en memoria (un signal). No se guarda en localStorage ni en
 *   sessionStorage, así un script inyectado (XSS) no puede leerlo del almacenamiento.
 * - El refresh token lo guarda el navegador en una cookie HttpOnly que JavaScript no puede leer;
 *   se envía solo a /api/auth gracias a su atributo Path.
 * - Al recargar la página, la sesión se recupera llamando a /api/auth/refresh.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly sesion = signal<Sesion | null>(null);
  private refrescoEnCurso: Observable<string | null> | null = null;

  readonly autenticado = computed(() => this.sesion() !== null);
  readonly usuario = computed(() => this.sesion()?.username ?? '');
  readonly rol = computed(() => this.sesion()?.rol ?? null);
  readonly esAdmin = computed(() => this.sesion()?.rol === 'ADMIN');

  get accessToken(): string | null {
    return this.sesion()?.accessToken ?? null;
  }

  login(username: string, password: string): Observable<void> {
    return this.http
      .post<TokenResponse>('/api/auth/login', { username, password }, { withCredentials: true })
      .pipe(
        tap((r) => this.guardar(r)),
        map(() => undefined)
      );
  }

  /**
   * Pide un access token nuevo con la cookie del refresh token. Si varias peticiones reciben 401 al
   * mismo tiempo, todas comparten la misma llamada de refresh (shareReplay) en lugar de lanzar varias.
   */
  refrescar(): Observable<string | null> {
    if (!this.refrescoEnCurso) {
      this.refrescoEnCurso = this.http
        .post<TokenResponse>('/api/auth/refresh', {}, { withCredentials: true })
        .pipe(
          tap((r) => this.guardar(r)),
          map((r) => r.accessToken),
          catchError(() => {
            this.sesion.set(null);
            return of(null);
          }),
          finalize(() => (this.refrescoEnCurso = null)),
          shareReplay(1)
        );
    }
    return this.refrescoEnCurso;
  }

  /** Se ejecuta al arrancar la app: si hay cookie de refresh válida, la sesión se restaura sola. */
  restaurarSesion(): Observable<unknown> {
    return this.refrescar();
  }

  logout(): void {
    this.http.post('/api/auth/logout', {}, { withCredentials: true }).subscribe({
      complete: () => this.terminar(),
      error: () => this.terminar(),
    });
  }

  /** Limpia la sesión local y regresa al login. */
  terminar(): void {
    this.sesion.set(null);
    this.router.navigate(['/login']);
  }

  private guardar(r: TokenResponse): void {
    this.sesion.set({
      username: r.username,
      rol: r.rol,
      accessToken: r.accessToken,
      expiraEn: Date.now() + r.expiresIn * 1000,
    });
  }
}
