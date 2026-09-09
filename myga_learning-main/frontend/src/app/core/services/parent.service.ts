import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AssessmentResponse, AttendanceSummary, GradeResponse,
  ObservationResponse, StudentPerformance, StudentSummary,
} from '../models/domain.models';

/**
 * The authenticated parent's own view. Every call is ownership-checked on the
 * server against the children linked to this parent.
 */
@Injectable({ providedIn: 'root' })
export class ParentService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  myChildren(): Observable<StudentSummary[]> {
    return this.http.get<StudentSummary[]>(`${this.base}/parent/me/children`);
  }
  performance(studentId: number): Observable<StudentPerformance> {
    return this.http.get<StudentPerformance>(`${this.base}/students/${studentId}/performance`);
  }
  grades(studentId: number): Observable<GradeResponse[]> {
    return this.http.get<GradeResponse[]>(`${this.base}/students/${studentId}/grades`);
  }
  attendance(studentId: number): Observable<AttendanceSummary> {
    return this.http.get<AttendanceSummary>(`${this.base}/students/${studentId}/attendance`);
  }
  observations(studentId: number): Observable<ObservationResponse[]> {
    return this.http.get<ObservationResponse[]>(`${this.base}/students/${studentId}/observations`);
  }
  assessments(studentId: number, upcoming = false): Observable<AssessmentResponse[]> {
    return this.http.get<AssessmentResponse[]>(
      `${this.base}/students/${studentId}/assessments?upcoming=${upcoming}`);
  }
}
