import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminDashboard, ParentDashboard, TeacherDashboard } from '../models/dashboard.models';

/** One aggregate call per role, matching the backend dashboard endpoints. */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  admin(): Observable<AdminDashboard> {
    return this.http.get<AdminDashboard>(`${this.base}/dashboard/admin`);
  }

  teacher(): Observable<TeacherDashboard> {
    return this.http.get<TeacherDashboard>(`${this.base}/dashboard/teacher`);
  }

  parent(): Observable<ParentDashboard> {
    return this.http.get<ParentDashboard>(`${this.base}/dashboard/parent`);
  }
}
