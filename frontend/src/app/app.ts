import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { AuthService } from './auth/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `
    @if (auth.autenticado()) {
      <header class="barra">
        <span>Sesión: <strong>{{ auth.usuario() }}</strong> ({{ auth.rol() }})</span>
        <button type="button" (click)="auth.logout()">Cerrar sesión</button>
      </header>
    }
    <router-outlet />
  `,
  styles: `
    .barra {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 1rem;
      max-width: 960px;
      margin: 0 auto;
      padding: 0.75rem 1.5rem 0;
      color: #5c564e;
    }
    .barra button {
      font: inherit;
      padding: 0.35rem 0.8rem;
      border: 1px solid #cfc6b8;
      border-radius: 8px;
      background: #fff;
      cursor: pointer;
    }
  `,
})
export class App {
  readonly auth = inject(AuthService);
}
