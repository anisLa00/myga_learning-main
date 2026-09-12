import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Role } from '../models/auth.models';
import {
  ClasseRequest,
  ClasseResponse,
  PageResponse,
  ParentRequest,
  ParentResponse,
  StudentRequest,
  StudentResponse,
  SubjectRequest,
  SubjectResponse,
  TeacherRequest,
  TeacherResponse,
  UserResponse,
} from '../models/domain.models';

/** Administrator management operations, mirroring the admin REST endpoints. */
@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  // --- students (paged + filtered) ---
  students(opts: { search?: string; classeId?: number | null; page?: number; size?: number } = {})
    : Observable<PageResponse<StudentResponse>> {
    let params = new HttpParams();
    if (opts.search) params = params.set('search', opts.search);
    if (opts.classeId != null) params = params.set('classeId', opts.classeId);
    if (opts.page != null) params = params.set('page', opts.page);
    if (opts.size != null) params = params.set('size', opts.size);
    return this.http.get<PageResponse<StudentResponse>>(`${this.base}/students`, { params });
  }
  createStudent(body: StudentRequest) { return this.http.post<StudentResponse>(`${this.base}/students`, body); }
  updateStudent(id: number, body: StudentRequest) { return this.http.put<StudentResponse>(`${this.base}/students/${id}`, body); }
  deleteStudent(id: number) { return this.http.delete<void>(`${this.base}/students/${id}`); }

  // --- classes ---
  classes() { return this.http.get<ClasseResponse[]>(`${this.base}/classes`); }
  createClasse(body: ClasseRequest) { return this.http.post<ClasseResponse>(`${this.base}/classes`, body); }
  updateClasse(id: number, body: ClasseRequest) { return this.http.put<ClasseResponse>(`${this.base}/classes/${id}`, body); }
  deleteClasse(id: number) { return this.http.delete<void>(`${this.base}/classes/${id}`); }

  // --- subjects ---
  subjects() { return this.http.get<SubjectResponse[]>(`${this.base}/subjects`); }
  createSubject(body: SubjectRequest) { return this.http.post<SubjectResponse>(`${this.base}/subjects`, body); }
  updateSubject(id: number, body: SubjectRequest) { return this.http.put<SubjectResponse>(`${this.base}/subjects/${id}`, body); }
  deleteSubject(id: number) { return this.http.delete<void>(`${this.base}/subjects/${id}`); }

  // --- teachers ---
  teachers() { return this.http.get<TeacherResponse[]>(`${this.base}/teachers`); }
  createTeacher(body: TeacherRequest) { return this.http.post<TeacherResponse>(`${this.base}/teachers`, body); }
  deleteTeacher(id: number) { return this.http.delete<void>(`${this.base}/teachers/${id}`); }
  assignSubject(id: number, subjectId: number) { return this.http.post<TeacherResponse>(`${this.base}/teachers/${id}/subjects/${subjectId}`, {}); }
  unassignSubject(id: number, subjectId: number) { return this.http.delete<TeacherResponse>(`${this.base}/teachers/${id}/subjects/${subjectId}`); }
  assignClasse(id: number, classeId: number) { return this.http.post<TeacherResponse>(`${this.base}/teachers/${id}/classes/${classeId}`, {}); }
  unassignClasse(id: number, classeId: number) { return this.http.delete<TeacherResponse>(`${this.base}/teachers/${id}/classes/${classeId}`); }

  // --- parents ---
  parents() { return this.http.get<ParentResponse[]>(`${this.base}/parents`); }
  createParent(body: ParentRequest) { return this.http.post<ParentResponse>(`${this.base}/parents`, body); }
  deleteParent(phone: number) { return this.http.delete<void>(`${this.base}/parents/${phone}`); }
  linkChild(phone: number, studentId: number) { return this.http.post<ParentResponse>(`${this.base}/parents/${phone}/students/${studentId}`, {}); }
  unlinkChild(phone: number, studentId: number) { return this.http.delete<ParentResponse>(`${this.base}/parents/${phone}/students/${studentId}`); }

  // --- accounts ---
  users(role?: Role) {
    let params = new HttpParams();
    if (role) params = params.set('role', role);
    return this.http.get<UserResponse[]>(`${this.base}/users`, { params });
  }
  setUserStatus(id: number, enabled: boolean) {
    return this.http.put<UserResponse>(`${this.base}/users/${id}/status`, { enabled });
  }

  /** Admin-set password for a locked-out account. */
  resetUserPassword(id: number, newPassword: string) {
    return this.http.put<UserResponse>(`${this.base}/users/${id}/password`, { newPassword });
  }
}
