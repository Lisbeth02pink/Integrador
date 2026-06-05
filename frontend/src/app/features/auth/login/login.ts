import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { finalize, timeout } from 'rxjs';
import { Auth } from '../../../core/services/auth';
import { ProfilesService } from '../../../core/services/profiles';

@Component({
  selector: 'app-login',
  imports: [FormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  username = '';
  password = '';
  loading = false;
  errorMessage = '';

  spans = Array.from({ length: 50 }, (_, i) => i);

  constructor(
    private router: Router,
    private authService: Auth,
    private profilesService: ProfilesService
  ) {}

  login() {
    this.loading = true;
    this.errorMessage = '';

    this.authService
      .login({
        username: this.username,
        password: this.password,
      })
      .pipe(
        timeout(10000),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (response) => {
          this.authService.saveSession(response);
          this.profilesService.listModules().subscribe({ error: () => undefined });
          this.profilesService.listProfiles().subscribe({ error: () => undefined });
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.errorMessage =
            error?.error?.message ||
            'No se pudo iniciar sesion. Verifica tus credenciales o que el backend este activo.';
        },
      });
  }
}
