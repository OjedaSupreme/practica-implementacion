import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from './auth.service';

/** Agrega el encabezado Authorization: Bearer <token> a una petición. */
function conToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

/**
 * Interceptor HTTP funcional.
 * 1. Adjunta el access token a toda petición hacia /api (excepto /api/auth).
 * 2. Si el backend responde 401 (token expirado o inválido), pide un token nuevo con el refresh
 *    token y repite la petición original UNA sola vez.
 * 3. Si la renovación falla, cierra la sesión local y manda al usuario al login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  const esApi = req.url.startsWith('/api/');
  const esAuth = req.url.startsWith('/api/auth/');
  if (!esApi || esAuth) {
    return next(req);
  }

  const token = auth.accessToken;
  const peticion = token ? conToken(req, token) : req;

  return next(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401) {
        return throwError(() => error);
      }
      return auth.refrescar().pipe(
        switchMap((nuevo) => {
          if (!nuevo) {
            auth.terminar();
            return throwError(() => error);
          }
          return next(conToken(req, nuevo));
        })
      );
    })
  );
};
