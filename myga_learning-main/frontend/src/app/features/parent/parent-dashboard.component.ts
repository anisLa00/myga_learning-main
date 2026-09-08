import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DashboardService } from '../../core/services/dashboard.service';
import { ParentDashboard } from '../../core/models/dashboard.models';

@Component({
  selector: 'app-parent-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1>Parent dashboard</h1>

    @if (data(); as d) {
      @if (d.children.length === 0) {
        <p class="muted">No children are linked to your account yet.</p>
      } @else {
        <h2>My children</h2>
        <div class="cards">
          @for (child of d.children; track child.id) {
            <button class="child-card" [class.active]="child.id === selectedId()" (click)="select(child.id)">
              <span class="name">{{ child.prenom }} {{ child.nom }}</span>
              <span class="muted">Age {{ child.age }}</span>
            </button>
          }
        </div>

        @if (selectedAttendance(); as att) {
          <h2>Attendance — {{ att.student.prenom }} {{ att.student.nom }}</h2>
          <div class="stats">
            <div class="stat"><span class="num">{{ att.attendancePercentage }}%</span><span class="label">Attendance</span></div>
            <div class="stat"><span class="num">{{ att.totalPresent }}</span><span class="label">Present</span></div>
            <div class="stat"><span class="num">{{ att.totalAbsent }}</span><span class="label">Absent</span></div>
            <div class="stat"><span class="num">{{ att.totalLate }}</span><span class="label">Late</span></div>
            <div class="stat"><span class="num">{{ att.totalExcused }}</span><span class="label">Excused</span></div>
          </div>
        }

        <h2>Latest grades</h2>
        @if (gradesForSelected().length === 0) {
          <p class="muted">No grades recorded yet.</p>
        } @else {
          <table>
            <thead><tr><th>Subject</th><th>Grade</th><th>Teacher</th><th>Date</th></tr></thead>
            <tbody>
              @for (g of gradesForSelected(); track g.id) {
                <tr>
                  <td>{{ g.subject.nom }}</td>
                  <td>{{ g.value }} / {{ g.maxValue }}</td>
                  <td>{{ g.teacherName || '—' }}</td>
                  <td>{{ g.date || '—' }}</td>
                </tr>
              }
            </tbody>
          </table>
        }

        <h2>Upcoming assessments</h2>
        @if (d.upcomingAssessments.length === 0) {
          <p class="muted">Nothing scheduled.</p>
        } @else {
          <table>
            <thead><tr><th>Title</th><th>Type</th><th>Subject</th><th>Date</th></tr></thead>
            <tbody>
              @for (a of d.upcomingAssessments; track a.id) {
                <tr>
                  <td>{{ a.title }}</td>
                  <td><span class="tag">{{ a.type }}</span></td>
                  <td>{{ a.subject.nom }}</td>
                  <td>{{ a.date || '—' }}</td>
                </tr>
              }
            </tbody>
          </table>
        }

        <h2>Teacher feedback</h2>
        @if (feedbackForSelected().length === 0) {
          <p class="muted">No feedback yet.</p>
        } @else {
          <ul class="feed">
            @for (o of feedbackForSelected(); track o.id) {
              <li>
                <strong>{{ o.teacherName || 'Teacher' }}</strong>
                <span class="tag">{{ o.type }}</span>
                <p class="muted">{{ o.message }}</p>
              </li>
            }
          </ul>
        }
      }

      <h2>Announcements</h2>
      @if (d.announcements.length === 0) {
        <p class="muted">Nothing new.</p>
      } @else {
        <ul class="feed">
          @for (a of d.announcements; track a.id) {
            <li><strong>{{ a.title }}</strong><p class="muted">{{ a.message }}</p></li>
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
export class ParentDashboardComponent implements OnInit {
  private readonly dashboard = inject(DashboardService);

  readonly data = signal<ParentDashboard | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly selectedId = signal<number | null>(null);

  /** Attendance stats for the selected child. */
  readonly selectedAttendance = computed(() => {
    const id = this.selectedId();
    return this.data()?.attendance.find((a) => a.student.id === id) ?? null;
  });

  readonly gradesForSelected = computed(() => {
    const id = this.selectedId();
    return (this.data()?.latestGrades ?? []).filter((g) => g.student.id === id);
  });

  readonly feedbackForSelected = computed(() => {
    const id = this.selectedId();
    return (this.data()?.recentFeedback ?? []).filter((o) => o.student.id === id);
  });

  ngOnInit(): void {
    this.dashboard.parent().subscribe({
      next: (d) => {
        this.data.set(d);
        // Select the first child so the page is useful immediately.
        if (d.children.length > 0) {
          this.selectedId.set(d.children[0].id);
        }
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load your dashboard.');
        this.loading.set(false);
      },
    });
  }

  select(id: number): void {
    this.selectedId.set(id);
  }
}
