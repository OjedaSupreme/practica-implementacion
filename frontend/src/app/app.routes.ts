import { Routes } from '@angular/router';

import { authGuard, invitadoGuard } from './auth/auth.guard';
import { LoginPage } from './pages/login/login';
import { TareasPage } from './pages/tareas/tareas';

export const routes: Routes = [
  { path: 'login', component: LoginPage, canActivate: [invitadoGuard] },
  { path: '', component: TareasPage, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];
