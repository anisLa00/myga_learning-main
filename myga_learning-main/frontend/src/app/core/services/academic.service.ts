import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ClasseSummary,
  GradeResponse,
  PageResponse,
  StudentResponse,
  StudentSummary,
  SubjectResponse,
} from '../models/domain.models';

/** Typed access to the academic REST endpoints. The JWT is attached by the interceptor. */
@Injectable({ providedIn: 'root' })
export class AcademicService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  // Admin — the students endpoint is paged; callers that only need the rows
  // can use getStudents(), while getStudentPage() exposes the page metadata.
  getStudentPage(
    opts: { search?: string; classeId?: number; page?: number; size?: number } = {}
  ): Observable<PageResponse<StudentResponse>> {
    let params = new HttpParams();
    if (opts.search) params = params.set('search', opts.search);
    if (opts.classeId != null) params = params.set('classeId', opts.classeId);
    if (opts.page != null) params = params.set('page', opts.page);
    if (opts.size != null) params = params.set('size', opts.size);
    return this.http.get<PageResponse<StudentResponse>>(`${this.base}/students`, { params });
  }

  getStudents(): Observable<StudentResponse[]> {
    return this.getStudentPage({ size: 100 }).pipe(map((page) => page.content));
  }

  // Parent portal
  getMyChildren(): Observable<StudentSummary[]> {
    return this.http.get<StudentSummary[]>(`${this.base}/parent/me/children`);
  }

  // Teacher portal
  getMyClasses(): Observable<ClasseSummary[]> {
    return this.http.get<ClasseSummary[]>(`${this.base}/teacher/me/classes`);
  }

  getMySubjects(): Observable<SubjectResponse[]> {
    return this.http.get<SubjectResponse[]>(`${this.base}/teacher/me/subjects`);
  }

  getStudentGrades(studentId: number): Observable<GradeResponse[]> {
    return this.http.get<GradeResponse[]>(`${this.base}/students/${studentId}/grades`);
  }
}
