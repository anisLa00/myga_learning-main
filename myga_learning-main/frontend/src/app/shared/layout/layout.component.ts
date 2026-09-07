import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

/** Authenticated shell: a top bar with the current user and a logout button. */
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  template: `
    <header class="topbar">
      <span class="brand">MYGA Learning</span>
      <span class="spacer"></span>
      @if (auth.user(); as user) {
        <span class="who">
          {{ user.prenom }} {{ user.nom }}
          <span class="role">{{ user.role }}</span>
        </span>
      }
      <button class="logout" (click)="logout()">Logout</button>
    </header>
    <main class="content">
      <router-outlet />
    </main>
  `,
  styles: [`
    .topbar { display: flex; align-items: center; gap: .75rem; padding: .75rem 1.25rem;
              background: #1e3a8a; color: #fff; }
    .brand { font-weight: 600; }
    .spacer { flex: 1; }
    .who { font-size: .85rem; opacity: .95; }
    .role { background: rgba(255,255,255,.2); border-radius: 999px; padding: .1rem .5rem; margin-left: .4rem; font-size: .7rem; }
    .logout { background: #fff; color: #1e3a8a; border: 0; border-radius: 8px; padding: .4rem .8rem; cursor: pointer; }
    .content { padding: 1.5rem; max-width: 960px; margin: 0 auto; }
  `],
})
export class LayoutComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
