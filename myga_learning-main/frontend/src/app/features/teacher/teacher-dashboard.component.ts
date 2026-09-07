import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { AcademicService } from '../../core/services/academic.service';
import { ClasseSummary, SubjectResponse } from '../../core/models/domain.models';

@Component({
  selector: 'app-teacher-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1>Teacher dashboard</h1>

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (error()) {
      <div class="alert">{{ error() }}</div>
    } @else {
      <div class="stats">
        <div class="stat"><span class="num">{{ classes().length }}</span><span class="label">My classes</span></div>
        <div class="stat"><span class="num">{{ subjects().length }}</span><span class="label">My subjects</span></div>
      </div>

      <h2>Assigned subjects</h2>
      @if (subjects().length === 0) {
        <p class="muted">No subjects assigned.</p>
      } @else {
        <ul class="chips">
          @for (s of subjects(); track s.id) {
            <li>{{ s.nom }}<span *ngIf="s.code"> ({{ s.code }})</span></li>
          }
        </ul>
      }

      <h2>Assigned classes</h2>
      @if (classes().length === 0) {
        <p class="muted">No classes assigned.</p>
      } @else {
        <ul class="chips">
          @for (c of classes(); track c.id) {
            <li>Room {{ c.salle }}</li>
          }
        </ul>
      }
    }
  `,
})
export class TeacherDashboardComponent implements OnInit {
  private readonly academic = inject(AcademicService);

  readonly classes = signal<ClasseSummary[]>([]);
  readonly subjects = signal<SubjectResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    forkJoin({
      classes: this.academic.getMyClasses(),
      subjects: this.academic.getMySubjects(),
    }).subscribe({
      next: ({ classes, subjects }) => {
        this.classes.set(classes);
        this.subjects.set(subjects);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load your workspace.');
        this.loading.set(false);
      },
    });
  }
}
