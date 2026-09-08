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
  assessmentId?: number | null;
  assessmentTitle?: string | null;
  semesterId?: number | null;
  semester?: string | null;
  academicYear?: string | null;
  value: number;
  maxValue: number;
  comment?: string;
  date?: string;
}

export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED';

export interface AttendanceResponse {
  id: number;
  student: StudentSummary;
  classe?: ClasseSummary | null;
  subject?: SubjectResponse | null;
  teacherId?: number;
  teacherName?: string;
  date?: string;
  status: AttendanceStatus;
  note?: string | null;
}

export interface AttendanceSummary {
  records: AttendanceResponse[];
  total: number;
  totalPresent: number;
  totalAbsent: number;
  totalLate: number;
  totalExcused: number;
  attendancePercentage: number;
}

export type ObservationType =
  | 'ACADEMIC' | 'BEHAVIOR' | 'PARTICIPATION' | 'HOMEWORK' | 'POSITIVE_FEEDBACK' | 'CONCERN';

export interface ObservationResponse {
  id: number;
  student: StudentSummary;
  teacherId?: number;
  teacherName?: string;
  subject?: SubjectResponse | null;
  type: ObservationType;
  message: string;
  date?: string;
  visibleToParents: boolean;
}

export type AssessmentType = 'EXAM' | 'QUIZ' | 'HOMEWORK' | 'PROJECT' | 'ORAL';

export interface AssessmentResponse {
  id: number;
  title: string;
  description?: string | null;
  subject: SubjectResponse;
  classe: ClasseSummary;
  teacherId?: number;
  teacherName?: string;
  type: AssessmentType;
  date?: string;
  maxGrade: number;
  semesterId?: number | null;
  semester?: string | null;
  academicYear?: string | null;
}

export type PerformanceTrend = 'IMPROVING' | 'STABLE' | 'DECLINING' | 'INSUFFICIENT_DATA';

export interface SubjectAverage {
  subject: SubjectResponse;
  gradeCount: number;
  average: number;
}

export interface SemesterAverage {
  semesterId: number;
  semester: string;
  academicYear?: string | null;
  gradeCount: number;
  average: number;
}

export interface StudentPerformance {
  student: StudentSummary;
  totalGrades: number;
  overallAverage?: number | null;
  subjectAverages: SubjectAverage[];
  semesterAverages: SemesterAverage[];
  trend: PerformanceTrend;
  trendDelta?: number | null;
}

export interface NotificationResponse {
  id: number;
  type: string;
  title: string;
  message: string;
  createdAt?: string;
  readAt?: string | null;
  read: boolean;
}
