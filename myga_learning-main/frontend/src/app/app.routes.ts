import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login.component';
import { LayoutComponent } from './shared/layout/layout.component';
import { HomeRedirectComponent } from './features/home-redirect.component';
import { AdminDashboardComponent } from './features/admin/admin-dashboard.component';
import { TeacherDashboardComponent } from './features/teacher/teacher-dashboard.component';
import { ParentDashboardComponent } from './features/parent/parent-dashboard.component';
import { ManageStudentsComponent } from './features/admin/manage/students-page.component';
import { ManageClassesComponent } from './features/admin/manage/classes-page.component';
import { ManageSubjectsComponent } from './features/admin/manage/subjects-page.component';
import { ManageTeachersComponent } from './features/admin/manage/teachers-page.component';
import { ManageParentsComponent } from './features/admin/manage/parents-page.component';
import { ManageUsersComponent } from './features/admin/manage/users-page.component';
import { TeacherAttendanceComponent } from './features/teacher/work/attendance-page.component';
import { TeacherGradesComponent } from './features/teacher/work/grades-page.component';
import { TeacherObservationsComponent } from './features/teacher/work/observations-page.component';
import { TeacherAssessmentsComponent } from './features/teacher/work/assessments-page.component';

const adminOnly = [roleGuard(['ADMIN'])];
const teacherOnly = [roleGuard(['TEACHER'])];

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'admin', component: AdminDashboardComponent, canActivate: adminOnly },
      { path: 'admin/students', component: ManageStudentsComponent, canActivate: adminOnly },
      { path: 'admin/classes', component: ManageClassesComponent, canActivate: adminOnly },
      { path: 'admin/subjects', component: ManageSubjectsComponent, canActivate: adminOnly },
      { path: 'admin/teachers', component: ManageTeachersComponent, canActivate: adminOnly },
      { path: 'admin/parents', component: ManageParentsComponent, canActivate: adminOnly },
      { path: 'admin/users', component: ManageUsersComponent, canActivate: adminOnly },

      { path: 'teacher', component: TeacherDashboardComponent, canActivate: teacherOnly },
      { path: 'teacher/attendance', component: TeacherAttendanceComponent, canActivate: teacherOnly },
      { path: 'teacher/grades', component: TeacherGradesComponent, canActivate: teacherOnly },
      { path: 'teacher/observations', component: TeacherObservationsComponent, canActivate: teacherOnly },
      { path: 'teacher/assessments', component: TeacherAssessmentsComponent, canActivate: teacherOnly },
      { path: 'parent', component: ParentDashboardComponent, canActivate: [roleGuard(['PARENT'])] },
      { path: '', pathMatch: 'full', component: HomeRedirectComponent },
    ],
  },
  { path: '**', redirectTo: '' },
];
