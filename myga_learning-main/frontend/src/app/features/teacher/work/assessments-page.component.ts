import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { TeacherService } from '../../../core/services/teacher.service';
import {
  AssessmentResponse, AssessmentType, ClasseSummary, SemesterResponse, SubjectResponse,
} from '../../../core/models/domain.models';

const TYPES: AssessmentType[] = ['EXAM', 'QUIZ', 'HOMEWORK', 'PROJECT', 'ORAL'];

@Component({
  selector: 'app-teacher-assessments',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h1>Assessments</h1>
    <p class="muted small">You can only schedule assessments for a class and subject assigned to you.</p>

    <form class="panel" [formGroup]="form" (ngSubmit)="save()">
      <h2>Schedule an assessment</h2>
      <div class="row">
        <label>Title<input formControlName="title" /></label>
        <label>Class
          <select formControlName="classeId">
            <option [ngValue]="null">— choose —</option>
            @for (c of classes(); track c.id) { <option [ngValue]="c.id">Room {{ c.salle }}</option> }
          </select>
        </label>
        <label>Subject
          <select formControlName="subjectId">
            <option [ngValue]="null">— choose —</option>
            @for (s of subjects(); track s.id) { <option [ngValue]="s.id">{{ s.nom }}</option> }
          </select>
        </label>
        <label>Type
          <select formControlName="type">
            @for (t of types; track t) { <option [ngValue]="t">{{ t }}</option> }
          </select>
        </label>
        <label>Date<input type="date" formControlName="date" /></label>
        <label>Max grade<input type="number" step="0.5" formControlName="maxGrade" /></label>
        <label>Semester
          <select formControlName="semesterId">
            <option [ngValue]="null">— none —</option>
            @for (s of semesters(); track s.id) { <option [ngValue]="s.id">{{ s.label }}</option> }
          </select>
        </label>
      </div>
      <div class="row">
        <label style="flex:1">Description<input formControlName="description" /></label>
      </div>
      @if (formError()) { <div class="alert">{{ formError() }}</div> }
      <div class="actions"><button type="submit" [disabled]="saving()">{{ saving() ? 'Saving…' : 'Schedule' }}</button></div>
    </form>

    <h2>My assessments</h2>
    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (items().length === 0) {
      <p class="muted">Nothing scheduled yet.</p>
    } @else {
      <table>
        <thead><tr><th>Title</th><th>Type</th><th>Subject</th><th>Class</th><th>Date</th><th>Max</th><th>Semester</th></tr></thead>
        <tbody>
          @for (a of items(); track a.id) {
            <tr>
              <td>{{ a.title }}</td>
              <td><span class="tag">{{ a.type }}</span></td>
              <td>{{ a.subject.nom }}</td>
              <td>Room {{ a.classe.salle }}</td>
              <td>{{ a.date }}</td>
              <td>{{ a.maxGrade }}</td>
              <td>{{ a.semester || '—' }} @if (a.academicYear) { <span class="muted small">({{ a.academicYear }})</span> }</td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class TeacherAssessmentsComponent implements OnInit {
  private readonly api = inject(TeacherService);
  private readonly fb = inject(FormBuilder);
  readonly types = TYPES;

  readonly classes = signal<ClasseSummary[]>([]);
  readonly subjects = signal<SubjectResponse[]>([]);
  readonly semesters = signal<SemesterResponse[]>([]);
  readonly items = signal<AssessmentResponse[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    classeId: this.fb.control<number | null>(null, Validators.required),
    subjectId: this.fb.control<number | null>(null, Validators.required),
    type: this.fb.control<AssessmentType>('EXAM', Validators.required),
    date: [new Date().toISOString().slice(0, 10)],
    maxGrade: this.fb.control<number | null>(20, [Validators.required, Validators.min(0.5)]),
    semesterId: this.fb.control<number | null>(null),
    description: [''],
  });

  ngOnInit(): void {
    forkJoin({ c: this.api.myClasses(), s: this.api.mySubjects(), sem: this.api.semesters() }).subscribe({
      next: ({ c, s, sem }) => { this.classes.set(c); this.subjects.set(s); this.semesters.set(sem); },
    });
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api.myAssessments().subscribe({
      next: (a) => { this.items.set(a); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.formError.set(null);
    const raw = this.form.getRawValue();
    const body: Record<string, unknown> = {
      title: raw.title, classeId: raw.classeId, subjectId: raw.subjectId,
      type: raw.type, date: raw.date, maxGrade: raw.maxGrade,
    };
    if (raw.semesterId) body['semesterId'] = raw.semesterId;
    if (raw.description) body['description'] = raw.description;
    this.api.createAssessment(body).subscribe({
      next: () => { this.saving.set(false); this.form.patchValue({ title: '', description: '' }); this.load(); },
      error: (e) => { this.saving.set(false); this.formError.set(e?.error?.message ?? 'Could not schedule the assessment.'); },
    });
  }
}
