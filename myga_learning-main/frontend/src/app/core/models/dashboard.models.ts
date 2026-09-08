import {
  AssessmentResponse,
  AttendanceResponse,
  ClasseSummary,
  GradeResponse,
  ObservationResponse,
  StudentSummary,
  SubjectResponse,
} from './domain.models';

export interface AnnouncementResponse {
  id: number;
  title: string;
  message: string;
  target: 'ALL' | 'PARENTS' | 'TEACHERS' | 'CLASS' | 'USER';
  classeId?: number | null;
  targetUserEmail?: string | null;
  createdByEmail?: string | null;
  createdAt?: string;
}

export interface AdminDashboard {
  totalStudents: number;
  totalTeachers: number;
  totalParents: number;
  totalClasses: number;
  totalSubjects: number;
  recentGrades: GradeResponse[];
  recentAttendance: AttendanceResponse[];
  recentAbsences: AttendanceResponse[];
  recentAnnouncements: AnnouncementResponse[];
}

export interface TeacherDashboard {
  classes: ClasseSummary[];
  subjects: SubjectResponse[];
  totalStudents: number;
  recentGrades: GradeResponse[];
  recentObservations: ObservationResponse[];
  upcomingAssessments: AssessmentResponse[];
  announcements: AnnouncementResponse[];
  unreadNotifications: number;
}

export interface ChildAttendanceStats {
  student: StudentSummary;
  total: number;
  totalPresent: number;
  totalAbsent: number;
  totalLate: number;
  totalExcused: number;
  attendancePercentage: number;
}

export interface ParentDashboard {
  children: StudentSummary[];
  attendance: ChildAttendanceStats[];
  latestGrades: GradeResponse[];
  recentAbsences: AttendanceResponse[];
  recentFeedback: ObservationResponse[];
  upcomingAssessments: AssessmentResponse[];
  announcements: AnnouncementResponse[];
  unreadNotifications: number;
}
