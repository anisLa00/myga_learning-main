import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AssessmentResponse,
  AttendanceResponse,
  AttendanceStatus,
  ClasseSummary,
  GradeResponse,
  ObservationResponse,
  ObservationType,
  PageResponse,
  SemesterResponse,
  StudentResponse,
  SubjectResponse,
} from '../models/domain.models';

export interface BulkAttendanceEntry {
  studentId: number;
  status: AttendanceStatus;
  note?: string | null;
}

/** The authenticated teacher's own workspace operations. */
@Injectable({ providedIn: 'root' })
export class TeacherService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  myClasses(): Observable<ClasseSummary[]> {
    return this.http.get<ClasseSummary[]>(`${this.base}/teacher/me/classes`);
  }
  mySubjects(): Observable<SubjectResponse[]> {
    return this.http.get<SubjectResponse[]>(`${this.base}/teacher/me/subjects`);
  }
  myAssessments(): Observable<AssessmentResponse[]> {
    return this.http.get<AssessmentResponse[]>(`${this.base}/teacher/me/assessments`);
  }
  semesters(): Observable<SemesterResponse[]> {
    return this.http.get<SemesterResponse[]>(`${this.base}/semesters`);
  }

  /** Students of one class (the backend scopes what a teacher may act on). */
  studentsInClass(classeId: number): Observable<PageResponse<StudentResponse>> {
    const params = new HttpParams().set('classeId', classeId).set('size', 200);
    return this.http.get<PageResponse<StudentResponse>>(`${this.base}/students`, { params });
  }

  // --- attendance ---
  classRegister(classeId: number, date: string): Observable<AttendanceResponse[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<AttendanceResponse[]>(`${this.base}/classes/${classeId}/attendance`, { params });
  }
  markClass(body: { classeId: number; subjectId?: number | null; date: string; entries: BulkAttendanceEntry[] }) {
    return this.http.post<AttendanceResponse[]>(`${this.base}/attendance/bulk`, body);
  }

  // --- grades ---
  searchGrades(filters: {
    classeId?: number | null; subjectId?: number | null; studentId?: number | null;
    semesterId?: number | null; assessmentId?: number | null; page?: number; size?: number;
  } = {}): Observable<PageResponse<GradeResponse>> {
    let params = new HttpParams();
    for (const [k, v] of Object.entries(filters)) {
      if (v !== null && v !== undefined) params = params.set(k, v);
    }
    return this.http.get<PageResponse<GradeResponse>>(`${this.base}/grades`, { params });
  }
  createGrade(body: Record<string, unknown>) { return this.http.post<GradeResponse>(`${this.base}/grades`, body); }
  updateGrade(id: number, body: Record<string, unknown>) { return this.http.put<GradeResponse>(`${this.base}/grades/${id}`, body); }
  deleteGrade(id: number) { return this.http.delete<void>(`${this.base}/grades/${id}`); }

  // --- observations ---
  createObservation(body: {
    studentId: number; subjectId?: number | null; type: ObservationType;
    message: string; visibleToParents: boolean;
  }) {
    return this.http.post<ObservationResponse>(`${this.base}/observations`, body);
  }
  studentObservations(studentId: number): Observable<ObservationResponse[]> {
    return this.http.get<ObservationResponse[]>(`${this.base}/students/${studentId}/observations`);
  }

  // --- assessments ---
  createAssessment(body: Record<string, unknown>) {
    return this.http.post<AssessmentResponse>(`${this.base}/assessments`, body);
  }
}
