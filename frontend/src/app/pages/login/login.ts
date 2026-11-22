import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  username = '';
  password = '';
  readonly enviando = signal(false);
  readonly error = signal('');

  entrar(): void {
    if (!this.username.trim() || !this.password) {
      this.error.set('Escribe usuario y contraseña.');
      return;
    }
    this.enviando.set(true);
    this.error.set('');

    this.auth.login(this.username.trim(), this.password).subscribe({
      next: () => {
        this.enviando.set(false);
        this.password = '';
        this.router.navigate(['/']);
      },
      error: (e: HttpErrorResponse) => {
        this.enviando.set(false);
        this.error.set(
          e.status === 401
            ? 'Usuario o contraseña incorrectos.'
            : 'No se pudo conectar con el servidor. Confirma que el backend esté en el puerto 8080.'
        );
      },
    });
  }
}
