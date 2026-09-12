import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

/** Authenticated shell: role-aware navigation, current user and logout. */
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topbar">
      <span class="brand">MYGA Learning</span>
      <span class="spacer"></span>
      @if (auth.user(); as user) {
        <a class="who" routerLink="/account" title="My account">
          {{ user.prenom }} {{ user.nom }}
          <span class="role">{{ user.role }}</span>
        </a>
      }
      <a class="bell" routerLink="/notifications" title="Notifications">
        🔔
        @if (notifications.unread() > 0) { <span class="badge">{{ notifications.unread() }}</span> }
      </a>
      <button class="logout" (click)="logout()">Logout</button>
    </header>

    @if (auth.user()?.role === 'ADMIN') {
      <nav class="subnav">
        <a routerLink="/admin" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Dashboard</a>
        <a routerLink="/admin/students" routerLinkActive="active">Students</a>
        <a routerLink="/admin/classes" routerLinkActive="active">Classes</a>
        <a routerLink="/admin/subjects" routerLinkActive="active">Subjects</a>
        <a routerLink="/admin/teachers" routerLinkActive="active">Teachers</a>
        <a routerLink="/admin/parents" routerLinkActive="active">Parents</a>
        <a routerLink="/admin/users" routerLinkActive="active">Accounts</a>
        <a routerLink="/announcements" routerLinkActive="active">Announcements</a>
      </nav>
    }

    @if (auth.user()?.role === 'TEACHER') {
      <nav class="subnav">
        <a routerLink="/teacher" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Dashboard</a>
        <a routerLink="/teacher/attendance" routerLinkActive="active">Attendance</a>
        <a routerLink="/teacher/grades" routerLinkActive="active">Grades</a>
        <a routerLink="/teacher/assessments" routerLinkActive="active">Assessments</a>
        <a routerLink="/teacher/observations" routerLinkActive="active">Observations</a>
        <a routerLink="/announcements" routerLinkActive="active">Announcements</a>
      </nav>
    }

    @if (auth.user()?.role === 'PARENT') {
      <nav class="subnav">
        <a routerLink="/parent" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Dashboard</a>
        <a routerLink="/parent/children" routerLinkActive="active">My children</a>
        <a routerLink="/announcements" routerLinkActive="active">Announcements</a>
      </nav>
    }

    <main class="content">
      <router-outlet />
    </main>
  `,
  styles: [`
    .topbar { display: flex; align-items: center; gap: .75rem; padding: .75rem 1.25rem;
              background: #1e3a8a; color: #fff; }
    .brand { font-weight: 600; }
    .spacer { flex: 1; }
    .who { font-size: .85rem; opacity: .95; color: inherit; text-decoration: none; }
    .who:hover { text-decoration: underline; }
    .role { background: rgba(255,255,255,.2); border-radius: 999px; padding: .1rem .5rem; margin-left: .4rem; font-size: .7rem; }
    .logout { background: #fff; color: #1e3a8a; border: 0; border-radius: 8px; padding: .4rem .8rem; cursor: pointer; }
    .bell { position: relative; text-decoration: none; font-size: 1.1rem; padding: .2rem .35rem; }
    .badge { position: absolute; top: -2px; right: -6px; background: #dc2626; color: #fff;
             border-radius: 999px; font-size: .65rem; padding: .05rem .3rem; font-weight: 700; }
    .subnav { display: flex; gap: .25rem; flex-wrap: wrap; padding: .5rem 1.25rem; background: #1e40af; }
    .subnav a { color: #dbeafe; text-decoration: none; font-size: .85rem; padding: .35rem .7rem; border-radius: 6px; }
    .subnav a:hover { background: rgba(255,255,255,.12); }
    .subnav a.active { background: #fff; color: #1e3a8a; font-weight: 600; }
    .content { padding: 1.5rem; max-width: 1040px; margin: 0 auto; }
  `],
})
export class LayoutComponent implements OnInit {
  readonly auth = inject(AuthService);
  readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.notifications.refreshUnread();
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
