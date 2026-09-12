import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

/** Lets any signed-in user change their own password. */
@Component({
  selector: 'app-account',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h1>My account</h1>

    @if (auth.user(); as user) {
      <div class="panel">
        <h2>Details</h2>
        <p class="muted">
          {{ user.prenom }} {{ user.nom }} · {{ user.email }} · {{ user.role }}
        </p>
        <p class="muted small">
          Accounts are created by the school administration. To change your email
          address or role, ask an administrator.
        </p>
      </div>
    }

    <form class="panel" [formGroup]="form" (ngSubmit)="submit()">
      <h2>Change password</h2>
      <p class="muted small">
        Signing in elsewhere ends those sessions: changing your password signs out
        every other device straight away.
      </p>

      <div class="row">
        <label>
          Current password
          <input type="password" formControlName="currentPassword" autocomplete="current-password" />
        </label>
        <label>
          New password
          <input type="password" formControlName="newPassword" autocomplete="new-password" />
        </label>
        <label>
          Confirm new password
          <input type="password" formControlName="confirmPassword" autocomplete="new-password" />
        </label>
      </div>

      <p class="muted small">At least 8 characters.</p>

      @if (error()) { <div class="alert">{{ error() }}</div> }
      @if (done()) { <div class="notice">Password changed.</div> }

      <div class="actions">
        <button type="submit" [disabled]="form.invalid || saving()">
          {{ saving() ? 'Saving…' : 'Change password' }}
        </button>
      </div>
    </form>
  `,
})
export class AccountComponent {
  readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly done = signal(false);

  readonly form = this.fb.nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required],
  });

  submit(): void {
    const { currentPassword, newPassword, confirmPassword } = this.form.getRawValue();
    this.done.set(false);

    if (newPassword !== confirmPassword) {
      this.error.set('The two new passwords do not match.');
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.auth.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.saving.set(false);
        this.done.set(true);
        this.form.reset();
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(
          err?.status === 401
            ? 'Your current password is not correct.'
            : err?.error?.message ?? 'Could not change the password.'
        );
      },
    });
  }
}
