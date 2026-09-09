import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotificationResponse } from '../models/domain.models';

/** Each user's own in-app notification inbox. */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  private readonly _unread = signal(0);
  /** Shared unread count, so the shell badge and the inbox stay in step. */
  readonly unread = this._unread.asReadonly();

  list(): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.base}/notifications/me`);
  }

  refreshUnread(): void {
    this.http.get<{ unread: number }>(`${this.base}/notifications/me/unread-count`)
      .subscribe({ next: (r) => this._unread.set(r.unread), error: () => this._unread.set(0) });
  }

  markRead(id: number): Observable<NotificationResponse> {
    return this.http.put<NotificationResponse>(`${this.base}/notifications/${id}/read`, {})
      .pipe(tap(() => this.refreshUnread()));
  }

  markAllRead(): Observable<void> {
    return this.http.put<void>(`${this.base}/notifications/me/read-all`, {})
      .pipe(tap(() => this._unread.set(0)));
  }
}
