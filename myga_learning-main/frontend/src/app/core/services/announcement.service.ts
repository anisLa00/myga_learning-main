import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AnnouncementResponse } from '../models/dashboard.models';

export interface AnnouncementRequest {
  title: string;
  message: string;
  target: 'ALL' | 'PARENTS' | 'TEACHERS' | 'CLASS' | 'USER';
  classeId?: number | null;
  userId?: number | null;
}

@Injectable({ providedIn: 'root' })
export class AnnouncementService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  /** Announcements relevant to the caller (any role). */
  mine(): Observable<AnnouncementResponse[]> {
    return this.http.get<AnnouncementResponse[]>(`${this.base}/announcements/me`);
  }
  /** Every announcement — admin management view. */
  all(): Observable<AnnouncementResponse[]> {
    return this.http.get<AnnouncementResponse[]>(`${this.base}/announcements`);
  }
  create(body: AnnouncementRequest): Observable<AnnouncementResponse> {
    return this.http.post<AnnouncementResponse>(`${this.base}/announcements`, body);
  }
}
