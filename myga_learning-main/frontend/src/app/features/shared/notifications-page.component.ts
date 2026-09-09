import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { NotificationService } from '../../core/services/notification.service';
import { NotificationResponse } from '../../core/models/domain.models';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1>Notifications</h1>

    <div class="toolbar">
      <span class="muted">{{ unreadCount() }} unread of {{ items().length }}</span>
      <span class="spacer"></span>
      <button (click)="markAll()" [disabled]="unreadCount() === 0">Mark all as read</button>
    </div>

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (error()) {
      <div class="alert">{{ error() }}</div>
    } @else if (items().length === 0) {
      <p class="muted">Nothing here yet.</p>
    } @else {
      <ul class="feed">
        @for (n of items(); track n.id) {
          <li [class.unread]="!n.read">
            <strong>{{ n.title }}</strong>
            <span class="tag">{{ n.type }}</span>
            @if (!n.read) { <span class="tag ok">new</span> }
            <p class="muted">{{ n.message }}</p>
            <div class="foot">
              <span class="muted small">{{ n.createdAt }}</span>
              @if (!n.read) {
                <button class="ghost" (click)="markRead(n)">Mark as read</button>
              }
            </div>
          </li>
        }
      </ul>
    }
  `,
  styles: [`
    .feed li.unread { border-left: 3px solid #1e3a8a; }
    .foot { display: flex; align-items: center; gap: .6rem; margin-top: .4rem; }
  `],
})
export class NotificationsComponent implements OnInit {
  private readonly api = inject(NotificationService);

  readonly items = signal<NotificationResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  unreadCount(): number { return this.items().filter((n) => !n.read).length; }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.api.list().subscribe({
      next: (n) => { this.items.set(n); this.loading.set(false); this.api.refreshUnread(); },
      error: () => { this.error.set('Could not load your notifications.'); this.loading.set(false); },
    });
  }

  markRead(n: NotificationResponse): void {
    this.api.markRead(n.id).subscribe({ next: () => this.load() });
  }

  markAll(): void {
    this.api.markAllRead().subscribe({ next: () => this.load() });
  }
}
