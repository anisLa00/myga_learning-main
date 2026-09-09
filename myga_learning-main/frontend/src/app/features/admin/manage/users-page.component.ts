import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';
import { UserResponse } from '../../../core/models/domain.models';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-manage-users',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1>Accounts</h1>
    <p class="muted small">Disabling an account blocks login without deleting any history. You cannot disable your own account.</p>

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
              </td>
            </tr>
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

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.api.users().subscribe({
      next: (u) => { this.items.set(u); this.loading.set(false); },
      error: () => { this.error.set('Could not load accounts.'); this.loading.set(false); },
    });
  }

  toggle(u: UserResponse): void {
    this.api.setUserStatus(u.id, !u.enabled).subscribe({
      next: () => { this.error.set(null); this.load(); },
      error: (e) => this.error.set(e?.error?.message ?? 'Could not change the account status.'),
    });
  }
}
