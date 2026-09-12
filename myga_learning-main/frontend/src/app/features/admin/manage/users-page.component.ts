import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { UserResponse } from '../../../core/models/domain.models';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-manage-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h1>Accounts</h1>
    <p class="muted small">Disabling an account blocks login without deleting any history. You cannot disable your own account.</p>
    <p class="muted small">
      There is no self-service password reset, by design. When someone is locked out,
      set a new password here and pass it on; every session they had open ends at once.
    </p>

    @if (resetDone()) { <div class="notice">New password set for {{ resetDone() }}.</div> }

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (error()) {
      <div class="alert">{{ error() }}</div>
    } @else {
      <table>
        <thead><tr><th>Email</th><th>Name</th><th>Role</th><th>Status</th><th></th></tr></thead>
        <tbody>
          @for (u of items(); track u.id) {
            <tr>
              <td>{{ u.email }}</td>
              <td>{{ u.prenom }} {{ u.nom }}</td>
              <td><span class="tag">{{ u.role }}</span></td>
              <td>
                @if (u.enabled) { <span class="tag ok">enabled</span> }
                @else { <span class="tag private">disabled</span> }
              </td>
              <td class="right">
                @if (u.email !== auth.user()?.email) {
                  <button class="ghost" (click)="toggle(u)">{{ u.enabled ? 'Disable' : 'Enable' }}</button>
                } @else {
                  <span class="muted small">you</span>
                }
                <button class="ghost" (click)="startReset(u)">Reset password</button>
              </td>
            </tr>
            @if (resetting()?.id === u.id) {
              <tr>
                <td colspan="5">
                  <div class="assign">
                    <label class="inline">
                      New password for {{ u.email }}
                      <input type="text" [(ngModel)]="newPassword" placeholder="at least 8 characters" />
                    </label>
                    <button class="ghost" (click)="confirmReset(u)" [disabled]="newPassword.length < 8">Set password</button>
                    <button class="ghost" (click)="cancelReset()">Cancel</button>
                  </div>
                </td>
              </tr>
            }
          }
        </tbody>
      </table>
    }
  `,
})
export class ManageUsersComponent implements OnInit {
  private readonly api = inject(AdminService);
  readonly auth = inject(AuthService);

  readonly items = signal<UserResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly resetting = signal<UserResponse | null>(null);
  readonly resetDone = signal<string | null>(null);
  newPassword = '';

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.api.users().subscribe({
      next: (u) => { this.items.set(u); this.loading.set(false); },
      error: () => { this.error.set('Could not load accounts.'); this.loading.set(false); },
    });
  }

  startReset(u: UserResponse): void {
    this.newPassword = '';
    this.resetDone.set(null);
    this.resetting.set(u);
  }

  cancelReset(): void {
    this.resetting.set(null);
    this.newPassword = '';
  }

  confirmReset(u: UserResponse): void {
    this.api.resetUserPassword(u.id, this.newPassword).subscribe({
      next: () => {
        this.error.set(null);
        this.resetDone.set(u.email);
        this.cancelReset();
      },
      error: (e) => this.error.set(e?.error?.message ?? 'Could not reset the password.'),
    });
  }

  toggle(u: UserResponse): void {
    this.api.setUserStatus(u.id, !u.enabled).subscribe({
      next: () => { this.error.set(null); this.load(); },
      error: (e) => this.error.set(e?.error?.message ?? 'Could not change the account status.'),
    });
  }
}
