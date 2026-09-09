import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { ParentService } from '../../../core/services/parent.service';
import {
  AssessmentResponse, AttendanceSummary, GradeResponse,
  ObservationResponse, StudentPerformance, StudentSummary,
} from '../../../core/models/domain.models';

@Component({
  selector: 'app-parent-child',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1>My children</h1>

    @if (children().length === 0 && !loading()) {
      <p class="muted">No children are linked to your account yet.</p>
    } @else {
      <div class="cards">
        @for (c of children(); track c.id) {
          <button class="child-card" [class.active]="c.id === selectedId()" (click)="select(c.id)">
            <span class="name">{{ c.prenom }} {{ c.nom }}</span>
            <span class="muted">Age {{ c.age }}</span>
          </button>
        }
      </div>
    }

    @if (error()) { <div class="alert">{{ error() }}</div> }

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (selectedId() !== null) {

      <h2>Academic performance</h2>
      @if (perf(); as p) {
        @if (p.totalGrades === 0) {
          <p class="muted">No grades recorded yet, so there is nothing to average.</p>
        } @else {
          <div class="stats">
            <div class="stat">
              <span class="num">{{ p.overallAverage }}%</span><span class="label">Overall average</span>
            </div>
            <div class="stat">
              <span class="num">{{ p.totalGrades }}</span><span class="label">Grades</span>
            </div>
            <div class="stat">
              <span class="num" [class.up]="p.trend === 'IMPROVING'" [class.down]="p.trend === 'DECLINING'">
                {{ trendLabel(p) }}
              </span>
              <span class="label">Trend</span>
            </div>
          </div>

          <h3>By subject</h3>
          <table>
            <thead><tr><th>Subject</th><th>Grades</th><th>Average</th><th></th></tr></thead>
            <tbody>
              @for (s of p.subjectAverages; track s.subject.id) {
                <tr>
                  <td>{{ s.subject.nom }}</td>
                  <td>{{ s.gradeCount }}</td>
                  <td>{{ s.average }}%</td>
                  <td><span class="bar"><span class="fill" [style.width.%]="s.average"></span></span></td>
                </tr>
              }
            </tbody>
          </table>

          @if (p.semesterAverages.length > 0) {
            <h3>By semester</h3>
            <table>
              <thead><tr><th>Semester</th><th>Academic year</th><th>Grades</th><th>Average</th></tr></thead>
              <tbody>
                @for (s of p.semesterAverages; track s.semesterId) {
                  <tr>
                    <td>{{ s.semester }}</td>
                    <td>{{ s.academicYear || '—' }}</td>
                    <td>{{ s.gradeCount }}</td>
                    <td>{{ s.average }}%</td>
                  </tr>
                }
              </tbody>
            </table>
          }
        }
      }

      <h2>Attendance</h2>
      @if (attendance(); as a) {
        <div class="stats">
          <div class="stat"><span class="num">{{ a.attendancePercentage }}%</span><span class="label">Attendance</span></div>
          <div class="stat"><span class="num">{{ a.totalPresent }}</span><span class="label">Present</span></div>
          <div class="stat"><span class="num">{{ a.totalAbsent }}</span><span class="label">Absent</span></div>
          <div class="stat"><span class="num">{{ a.totalLate }}</span><span class="label">Late</span></div>
          <div class="stat"><span class="num">{{ a.totalExcused }}</span><span class="label">Excused</span></div>
        </div>
        @if (a.records.length > 0) {
          <table>
            <thead><tr><th>Date</th><th>Status</th><th>Class</th><th>Note</th></tr></thead>
            <tbody>
              @for (r of a.records; track r.id) {
                <tr>
                  <td>{{ r.date }}</td>
                  <td><span class="tag" [class.private]="r.status === 'ABSENT'">{{ r.status }}</span></td>
                  <td>{{ r.classe ? 'Room ' + r.classe.salle : '—' }}</td>
                  <td>{{ r.note || '—' }}</td>
                </tr>
              }
            </tbody>
          </table>
        }
      }

      <h2>All grades</h2>
      @if (grades().length === 0) {
        <p class="muted">No grades recorded yet.</p>
      } @else {
        <table>
          <thead><tr><th>Subject</th><th>Assessment</th><th>Grade</th><th>Semester</th><th>Teacher</th><th>Date</th></tr></thead>
          <tbody>
            @for (g of grades(); track g.id) {
              <tr>
                <td>{{ g.subject.nom }}</td>
                <td>{{ g.assessmentTitle || '—' }}</td>
                <td>{{ g.value }} / {{ g.maxValue }}</td>
                <td>{{ g.semester || '—' }}</td>
                <td>{{ g.teacherName || '—' }}</td>
                <td>{{ g.date }}</td>
              </tr>
            }
          </tbody>
        </table>
      }

      <h2>Upcoming assessments</h2>
      @if (assessments().length === 0) {
        <p class="muted">Nothing scheduled.</p>
      } @else {
        <table>
          <thead><tr><th>Title</th><th>Type</th><th>Subject</th><th>Date</th><th>Out of</th></tr></thead>
          <tbody>
            @for (a of assessments(); track a.id) {
              <tr>
                <td>{{ a.title }}</td>
                <td><span class="tag">{{ a.type }}</span></td>
                <td>{{ a.subject.nom }}</td>
                <td>{{ a.date }}</td>
                <td>{{ a.maxGrade }}</td>
              </tr>
            }
          </tbody>
        </table>
      }

      <h2>Teacher feedback</h2>
      @if (observations().length === 0) {
        <p class="muted">No feedback yet.</p>
      } @else {
        <ul class="feed">
          @for (o of observations(); track o.id) {
            <li>
              <strong>{{ o.teacherName || 'Teacher' }}</strong>
              <span class="tag">{{ o.type }}</span>
              <p class="muted">{{ o.message }}</p>
              <span class="muted small">{{ o.date }}</span>
            </li>
          }
        </ul>
      }
    }
  `,
  styles: [`
    h3 { font-size: .95rem; color: #4b5563; margin: 1rem 0 .5rem; }
    .num.up { color: #166534; }
    .num.down { color: #b91c1c; }
    .bar { display: block; width: 140px; height: 8px; background: #e5e7eb; border-radius: 999px; overflow: hidden; }
    .fill { display: block; height: 100%; background: #1e3a8a; }
  `],
})
export class ParentChildComponent implements OnInit {
  private readonly api = inject(ParentService);

  readonly children = signal<StudentSummary[]>([]);
  readonly selectedId = signal<number | null>(null);
  readonly perf = signal<StudentPerformance | null>(null);
  readonly grades = signal<GradeResponse[]>([]);
  readonly attendance = signal<AttendanceSummary | null>(null);
  readonly observations = signal<ObservationResponse[]>([]);
  readonly assessments = signal<AssessmentResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.myChildren().subscribe({
      next: (c) => {
        this.children.set(c);
        if (c.length > 0) this.select(c[0].id);
        else this.loading.set(false);
      },
      error: () => { this.error.set('Could not load your children.'); this.loading.set(false); },
    });
  }

  select(id: number): void {
    this.selectedId.set(id);
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      perf: this.api.performance(id),
      grades: this.api.grades(id),
      att: this.api.attendance(id),
      obs: this.api.observations(id),
      ass: this.api.assessments(id, true),
    }).subscribe({
      next: ({ perf, grades, att, obs, ass }) => {
        this.perf.set(perf); this.grades.set(grades); this.attendance.set(att);
        this.observations.set(obs); this.assessments.set(ass);
        this.loading.set(false);
      },
      error: () => { this.error.set('Could not load this child\'s record.'); this.loading.set(false); },
    });
  }

  trendLabel(p: StudentPerformance): string {
    if (p.trend === 'INSUFFICIENT_DATA') return 'n/a';
    const delta = p.trendDelta ?? 0;
    const sign = delta > 0 ? '+' : '';
    return `${p.trend === 'IMPROVING' ? '▲' : p.trend === 'DECLINING' ? '▼' : '■'} ${sign}${delta}`;
  }
}
