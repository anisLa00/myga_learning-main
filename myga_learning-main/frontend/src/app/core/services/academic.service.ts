import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ClasseSummary,
  GradeResponse,
  StudentResponse,
  StudentSummary,
  SubjectResponse,
} from '../models/domain.models';

/** Typed access to the academic REST endpoints. The JWT is attached by the interceptor. */
@Injectable({ providedIn: 'root' })
export class AcademicService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  // Admin
  getStudents(): Observable<StudentResponse[]> {
    return this.http.get<StudentResponse[]>(`${this.base}/students`);
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
