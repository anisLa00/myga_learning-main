/** Envelope returned by the paged list endpoints. */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ClasseSummary {
  id: number;
  salle: number;
}

export interface StudentSummary {
  id: number;
  nom: string;
  prenom: string;
  age: number;
}

export interface ParentSummary {
  phone: number;
  nom: string;
  prenom: string;
  email: string;
}

export interface StudentResponse extends StudentSummary {
  classe?: ClasseSummary | null;
  parents?: ParentSummary[];
}

export interface SubjectResponse {
  id: number;
  nom: string;
  code?: string;
}

export interface GradeResponse {
  id: number;
  student: StudentSummary;
  subject: SubjectResponse;
  teacherId?: number;
  teacherName?: string;
  value: number;
  maxValue: number;
  comment?: string;
  date?: string;
}
