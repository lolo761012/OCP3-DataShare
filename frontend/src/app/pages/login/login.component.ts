import { Component, DestroyRef, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { HeaderComponent } from '../../shared/header/header.component';
import { AuthApiService } from '../../core/service/auth-api.service';
import { AuthService } from '../../core/service/auth.service';
import { LoginRequest } from '../../core/models/login-request.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, HeaderComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private formBuilder = inject(FormBuilder);
  private authApiService = inject(AuthApiService);
  private authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);
  private router = inject(Router);

  loginForm: FormGroup = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  submitted = false;

  loading = signal(false);
  success = signal(false);
  errorMessage = signal('');
  accountCreated = signal(
    window.history.state?.['accountCreated'] === true
  );

  get form() {
    return this.loginForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;
    this.accountCreated.set(false);
    this.success.set(false);
    this.errorMessage.set('');

    if (this.loginForm.invalid) {
      return;
    }

    this.loading.set(true);

    const request: LoginRequest = {
      email: this.loginForm.get('email')?.value,
      password: this.loginForm.get('password')?.value
    };

    this.authApiService.login(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.loading.set(false);
          this.authService.saveToken(response.token);
          this.success.set(true);
          this.router.navigate(['/myspace']);
        },
        error: (error) => {
          this.loading.set(false);
          this.success.set(false);
          this.errorMessage.set(error.error?.message ?? 'Serveur indisponible.');
        }
      });
  }
}