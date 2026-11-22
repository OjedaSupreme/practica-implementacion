import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

/** Protege rutas del frontend: sin sesión, redirige a /login. (La protección real está en el backend.) */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? true : router.createUrlTree(['/login']);
};

/** Evita mostrar el login a quien ya inició sesión. */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? router.createUrlTree(['/']) : true;
};
