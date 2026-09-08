import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { DashboardService } from '../../core/services/dashboard.service';
import { TeacherDashboard } from '../../core/models/dashboard.models';

@Component({
  selector: 'app-teacher-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1>Teacher dashboard</h1>

    @if (data(); as d) {
      <div class="stats">
        <div class="stat"><span class="num">{{ d.classes.length }}</span><span class="label">My classes</span></div>
        <div class="stat"><span class="num">{{ d.subjects.length }}</span><span class="label">My subjects</span></div>
        <div class="stat"><span class="num">{{ d.totalStudents }}</span><span class="label">My students</span></div>
        <div class="stat"><span class="num">{{ d.unreadNotifications }}</span><span class="label">Unread</span></div>
      </div>

      <h2>My subjects</h2>
      @if (d.subjects.length === 0) {
        <p class="muted">No subjects assigned.</p>
      } @else {
        <ul class="chips">
          @for (s of d.subjects; track s.id) { <li>{{ s.nom }}</li> }
        </ul>
      }

      <h2>My classes</h2>
      @if (d.classes.length === 0) {
        <p class="muted">No classes assigned.</p>
      } @else {
        <ul class="chips">
          @for (c of d.classes; track c.id) { <li>Room {{ c.salle }}</li> }
        </ul>
      }

      <h2>Upcoming assessments</h2>
      @if (d.upcomingAssessments.length === 0) {
        <p class="muted">Nothing scheduled.</p>
      } @else {
        <table>
          <thead><tr><th>Title</th><th>Type</th><th>Subject</th><th>Class</th><th>Date</th></tr></thead>
          <tbody>
            @for (a of d.upcomingAssessments; track a.id) {
              <tr>
                <td>{{ a.title }}</td>
                <td><span class="tag">{{ a.type }}</span></td>
                <td>{{ a.subject.nom }}</td>
                <td>Room {{ a.classe.salle }}</td>
                <td>{{ a.date || '—' }}</td>
              </tr>
            }
          </tbody>
        </table>
      }

      <h2>Recent grades I recorded</h2>
      @if (d.recentGrades.length === 0) {
        <p class="muted">No grades recorded yet.</p>
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

      <h2>Recent observations</h2>
      @if (d.recentObservations.length === 0) {
        <p class="muted">No observations recorded.</p>
      } @else {
        <ul class="feed">
          @for (o of d.recentObservations; track o.id) {
            <li>
              <strong>{{ o.student.prenom }} {{ o.student.nom }}</strong>
              <span class="tag">{{ o.type }}</span>
              @if (!o.visibleToParents) { <span class="tag private">private</span> }
              <p class="muted">{{ o.message }}</p>
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
export class TeacherDashboardComponent implements OnInit {
  private readonly dashboard = inject(DashboardService);

  readonly data = signal<TeacherDashboard | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.dashboard.teacher().subscribe({
      next: (d) => {
        this.data.set(d);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load your workspace.');
        this.loading.set(false);
      },
    });
  }
}
