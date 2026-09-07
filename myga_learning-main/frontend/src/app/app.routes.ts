import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards/auth.guard';
import { LoginComponent } from './features/auth/login.component';
import { LayoutComponent } from './shared/layout/layout.component';
import { HomeRedirectComponent } from './features/home-redirect.component';
import { AdminDashboardComponent } from './features/admin/admin-dashboard.component';
import { TeacherDashboardComponent } from './features/teacher/teacher-dashboard.component';
import { ParentDashboardComponent } from './features/parent/parent-dashboard.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'admin', component: AdminDashboardComponent, canActivate: [roleGuard(['ADMIN'])] },
      { path: 'teacher', component: TeacherDashboardComponent, canActivate: [roleGuard(['TEACHER'])] },
      { path: 'parent', component: ParentDashboardComponent, canActivate: [roleGuard(['PARENT'])] },
      { path: '', pathMatch: 'full', component: HomeRedirectComponent },
    ],
  },
  { path: '**', redirectTo: '' },
];
