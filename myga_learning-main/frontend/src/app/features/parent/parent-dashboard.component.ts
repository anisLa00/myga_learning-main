import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { AcademicService } from '../../core/services/academic.service';
import { GradeResponse, StudentSummary } from '../../core/models/domain.models';

@Component({
  selector: 'app-parent-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1>Parent dashboard</h1>

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (error()) {
      <div class="alert">{{ error() }}</div>
    } @else if (children().length === 0) {
      <p class="muted">No children are linked to your account yet.</p>
    } @else {
      <h2>My children</h2>
      <div class="cards">
        @for (child of children(); track child.id) {
          <button class="child-card" [class.active]="child.id === selectedId()" (click)="select(child)">
            <span class="name">{{ child.prenom }} {{ child.nom }}</span>
            <span class="muted">Age {{ child.age }}</span>
          </button>
        }
      </div>

      @if (selectedId() !== null) {
        <h2>Grades</h2>
        @if (gradesLoading()) {
          <p class="muted">Loading grades…</p>
        } @else if (gradesError()) {
          <div class="alert">{{ gradesError() }}</div>
        } @else if (grades().length === 0) {
          <p class="muted">No grades recorded yet.</p>
        } @else {
          <table>
            <thead>
              <tr><th>Subject</th><th>Grade</th><th>Teacher</th><th>Date</th><th>Comment</th></tr>
            </thead>
            <tbody>
              @for (g of grades(); track g.id) {
                <tr>
                  <td>{{ g.subject.nom }}</td>
                  <td>{{ g.value }} / {{ g.maxValue }}</td>
                  <td>{{ g.teacherName || '—' }}</td>
                  <td>{{ g.date || '—' }}</td>
                  <td>{{ g.comment || '—' }}</td>
                </tr>
              }
            </tbody>
          </table>
        }
      }
    }
  `,
})
export class ParentDashboardComponent implements OnInit {
  private readonly academic = inject(AcademicService);

  readonly children = signal<StudentSummary[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly selectedId = signal<number | null>(null);
  readonly grades = signal<GradeResponse[]>([]);
  readonly gradesLoading = signal(false);
  readonly gradesError = signal<string | null>(null);

  ngOnInit(): void {
    this.academic.getMyChildren().subscribe({
      next: (list) => {
        this.children.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load your children.');
        this.loading.set(false);
      },
    });
  }

  select(child: StudentSummary): void {
    this.selectedId.set(child.id);
    this.gradesLoading.set(true);
    this.gradesError.set(null);
    this.grades.set([]);
    this.academic.getStudentGrades(child.id).subscribe({
      next: (list) => {
        this.grades.set(list);
        this.gradesLoading.set(false);
      },
      error: () => {
        this.gradesError.set('Could not load grades.');
        this.gradesLoading.set(false);
      },
    });
  }
}
