import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="login-wrap">
      <form class="card" [formGroup]="form" (ngSubmit)="submit()">
        <h1>MYGA Learning</h1>
        <p class="subtitle">School Management Platform</p>

        <label>
          Email
          <input type="email" formControlName="email" autocomplete="username" placeholder="admin@myga.local" />
        </label>
        @if (showError('email')) {
          <small class="field-error">A valid email is required.</small>
        }

        <label>
          Password
          <input type="password" formControlName="password" autocomplete="current-password" placeholder="••••••••" />
        </label>
        @if (showError('password')) {
          <small class="field-error">Password is required.</small>
        }

        @if (error()) {
          <div class="alert">{{ error() }}</div>
        }

        <button type="submit" [disabled]="loading()">
          {{ loading() ? 'Signing in…' : 'Sign in' }}
        </button>

        <p class="hint">Accounts are created by the school administration. There is no public sign-up.</p>
      </form>
    </div>
  `,
  styles: [`
    .login-wrap { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 1rem; }
    .card { width: 100%; max-width: 360px; background: #fff; border: 1px solid #e5e7eb; border-radius: 12px;
            padding: 2rem; box-shadow: 0 10px 30px rgba(0,0,0,.06); display: flex; flex-direction: column; gap: .5rem; }
    h1 { margin: 0; font-size: 1.4rem; color: #1e3a8a; }
    .subtitle { margin: 0 0 1rem; color: #6b7280; font-size: .9rem; }
    label { display: flex; flex-direction: column; gap: .35rem; font-size: .85rem; color: #374151; margin-top: .5rem; }
    input { padding: .6rem .7rem; border: 1px solid #d1d5db; border-radius: 8px; font-size: .95rem; }
    input:focus { outline: 2px solid #93c5fd; border-color: #93c5fd; }
    button { margin-top: 1rem; padding: .7rem; background: #1e3a8a; color: #fff; border: 0; border-radius: 8px;
             font-size: .95rem; cursor: pointer; }
    button:disabled { opacity: .6; cursor: not-allowed; }
    .field-error { color: #b91c1c; }
    .alert { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; padding: .6rem .7rem; border-radius: 8px;
             font-size: .85rem; margin-top: .5rem; }
    .hint { margin-top: 1rem; font-size: .75rem; color: #9ca3af; text-align: center; }
  `],
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  showError(control: 'email' | 'password'): boolean {
    const c = this.form.controls[control];
    return c.invalid && (c.touched || c.dirty);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.router.navigate([this.auth.homePathForRole(res.role)]);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message ?? 'Login failed. Please check your credentials.');
      },
    });
  }
}
