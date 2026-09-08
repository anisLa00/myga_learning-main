import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { DashboardService } from '../../core/services/dashboard.service';
import { AdminDashboard } from '../../core/models/dashboard.models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1>Admin dashboard</h1>

    @if (data(); as d) {
      <div class="stats">
        <div class="stat"><span class="num">{{ d.totalStudents }}</span><span class="label">Students</span></div>
        <div class="stat"><span class="num">{{ d.totalTeachers }}</span><span class="label">Teachers</span></div>
        <div class="stat"><span class="num">{{ d.totalParents }}</span><span class="label">Parents</span></div>
        <div class="stat"><span class="num">{{ d.totalClasses }}</span><span class="label">Classes</span></div>
        <div class="stat"><span class="num">{{ d.totalSubjects }}</span><span class="label">Subjects</span></div>
      </div>

      <h2>Recent absences</h2>
      @if (d.recentAbsences.length === 0) {
        <p class="muted">No absences recorded.</p>
      } @else {
        <table>
          <thead><tr><th>Student</th><th>Class</th><th>Date</th></tr></thead>
          <tbody>
            @for (a of d.recentAbsences; track a.id) {
              <tr>
                <td>{{ a.student.prenom }} {{ a.student.nom }}</td>
                <td>{{ a.classe ? 'Room ' + a.classe.salle : '—' }}</td>
                <td>{{ a.date || '—' }}</td>
              </tr>
            }
          </tbody>
        </table>
      }

      <h2>Recent grades</h2>
      @if (d.recentGrades.length === 0) {
        <p class="muted">No grades recorded.</p>
      } @else {
        <table>
          <thead><tr><th>Student</th><th>Subject</th><th>Grade</th><th>Date</th></tr></thead>
          <tbody>
            @for (g of d.recentGrades; track g.id) {
              <tr>
                <td>{{ g.student.prenom }} {{ g.student.nom }}</td>
                <td>{{ g.subject.nom }}</td>
                <td>{{ g.value }} / {{ g.maxValue }}</td>
                <td>{{ g.date || '—' }}</td>
              </tr>
            }
          </tbody>
        </table>
      }

      <h2>Announcements</h2>
      @if (d.recentAnnouncements.length === 0) {
        <p class="muted">No announcements published.</p>
      } @else {
        <ul class="feed">
          @for (a of d.recentAnnouncements; track a.id) {
            <li>
              <strong>{{ a.title }}</strong>
              <span class="tag">{{ a.target }}</span>
              <p class="muted">{{ a.message }}</p>
            </li>
          }
        </ul>
      }
    } @else if (error()) {
      <div class="alert">{{ error() }}</div>
    } @else {
      <p class="muted">Loading…</p>
    }
  `,
})
export class AdminDashboardComponent implements OnInit {
  private readonly dashboard = inject(DashboardService);

  readonly data = signal<AdminDashboard | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.dashboard.admin().subscribe({
      next: (d) => {
        this.data.set(d);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load the dashboard.');
        this.loading.set(false);
      },
    });
  }
}
