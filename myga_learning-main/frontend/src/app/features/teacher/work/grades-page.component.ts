import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { TeacherService } from '../../../core/services/teacher.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  AssessmentResponse, ClasseSummary, GradeResponse, SemesterResponse,
  StudentResponse, SubjectResponse,
} from '../../../core/models/domain.models';

@Component({
  selector: 'app-teacher-grades',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <h1>Grades</h1>

    <form class="panel" [formGroup]="form" (ngSubmit)="save()">
      <h2>{{ editingId() ? 'Edit grade' : 'Record a grade' }}</h2>
      <p class="muted small">
        Pick an assessment to inherit its subject, maximum grade and semester, or set them yourself.
      </p>
      <div class="row">
        <label>Class
          <select [ngModel]="entryClasseId()" (ngModelChange)="onEntryClass($event)" [ngModelOptions]="{standalone: true}">
            <option [ngValue]="null">— choose —</option>
            @for (c of classes(); track c.id) { <option [ngValue]="c.id">Room {{ c.salle }}</option> }
          </select>
        </label>
        <label>Student
          <select formControlName="studentId">
            <option [ngValue]="null">— choose —</option>
            @for (s of entryStudents(); track s.id) { <option [ngValue]="s.id">{{ s.prenom }} {{ s.nom }}</option> }
          </select>
        </label>
        <label>Assessment
          <select formControlName="assessmentId">
            <option [ngValue]="null">— none —</option>
            @for (a of assessments(); track a.id) { <option [ngValue]="a.id">{{ a.title }} ({{ a.maxGrade }})</option> }
          </select>
        </label>
        <label>Subject
          <select formControlName="subjectId">
            <option [ngValue]="null">— from assessment —</option>
            @for (s of subjects(); track s.id) { <option [ngValue]="s.id">{{ s.nom }}</option> }
          </select>
        </label>
        <label>Value<input type="number" step="0.5" formControlName="value" /></label>
        <label>Out of<input type="number" step="0.5" formControlName="maxValue" placeholder="from assessment" /></label>
        <label>Semester
          <select formControlName="semesterId">
            <option [ngValue]="null">— none —</option>
            @for (s of semesters(); track s.id) { <option [ngValue]="s.id">{{ s.label }}</option> }
          </select>
        </label>
        <label>Comment<input formControlName="comment" /></label>
      </div>
      @if (formError()) { <div class="alert">{{ formError() }}</div> }
      <div class="actions">
        <button type="submit" [disabled]="saving()">{{ saving() ? 'Saving…' : (editingId() ? 'Update' : 'Record') }}</button>
        @if (editingId()) { <button type="button" class="ghost" (click)="cancelEdit()">Cancel</button> }
      </div>
    </form>

    <h2>My students' grades</h2>
    <div class="toolbar">
      <label class="inline">Class
        <select [ngModel]="filterClasseId()" (ngModelChange)="filterClasseId.set($event); load()">
          <option [ngValue]="null">all</option>
          @for (c of classes(); track c.id) { <option [ngValue]="c.id">Room {{ c.salle }}</option> }
        </select>
      </label>
      <label class="inline">Subject
        <select [ngModel]="filterSubjectId()" (ngModelChange)="filterSubjectId.set($event); load()">
          <option [ngValue]="null">all</option>
          @for (s of subjects(); track s.id) { <option [ngValue]="s.id">{{ s.nom }}</option> }
        </select>
      </label>
      <label class="inline">Semester
        <select [ngModel]="filterSemesterId()" (ngModelChange)="filterSemesterId.set($event); load()">
          <option [ngValue]="null">all</option>
          @for (s of semesters(); track s.id) { <option [ngValue]="s.id">{{ s.label }}</option> }
        </select>
      </label>
      <span class="spacer"></span>
      <span class="muted">{{ total() }} grade(s)</span>
    </div>

    @if (error()) { <div class="alert">{{ error() }}</div> }

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (items().length === 0) {
      <p class="muted">No grades match these filters.</p>
    } @else {
      <table>
        <thead><tr><th>Student</th><th>Subject</th><th>Assessment</th><th>Grade</th><th>Semester</th><th>Recorded by</th><th></th></tr></thead>
        <tbody>
          @for (g of items(); track g.id) {
            <tr>
              <td>{{ g.student.prenom }} {{ g.student.nom }}</td>
              <td>{{ g.subject.nom }}</td>
              <td>{{ g.assessmentTitle || '—' }}</td>
              <td>{{ g.value }} / {{ g.maxValue }}</td>
              <td>{{ g.semester || '—' }}</td>
              <td>{{ g.teacherName || '—' }}</td>
              <td class="right">
                @if (isMine(g)) {
                  <button class="ghost" (click)="edit(g)">Edit</button>
                  <button class="danger" (click)="remove(g)">Delete</button>
                } @else {
                  <span class="muted small">another teacher</span>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
      <div class="pager">
        <button class="ghost" [disabled]="page() === 0" (click)="go(page() - 1)">Previous</button>
        <span class="muted">Page {{ page() + 1 }} of {{ totalPages() || 1 }}</span>
        <button class="ghost" [disabled]="page() + 1 >= totalPages()" (click)="go(page() + 1)">Next</button>
      </div>
    }
  `,
})
export class TeacherGradesComponent implements OnInit {
  private readonly api = inject(TeacherService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly classes = signal<ClasseSummary[]>([]);
  readonly subjects = signal<SubjectResponse[]>([]);
  readonly semesters = signal<SemesterResponse[]>([]);
  readonly assessments = signal<AssessmentResponse[]>([]);
  readonly entryStudents = signal<StudentResponse[]>([]);
  readonly entryClasseId = signal<number | null>(null);

  readonly items = signal<GradeResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly formError = signal<string | null>(null);
  readonly saving = signal(false);
  readonly editingId = signal<number | null>(null);

  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly total = signal(0);
  readonly filterClasseId = signal<number | null>(null);
  readonly filterSubjectId = signal<number | null>(null);
  readonly filterSemesterId = signal<number | null>(null);

  /** The teacher's own name, used to tell their grades apart in the list. */
  private readonly myName = signal<string>('');

  readonly form = this.fb.nonNullable.group({
    studentId: this.fb.control<number | null>(null, Validators.required),
    assessmentId: this.fb.control<number | null>(null),
    subjectId: this.fb.control<number | null>(null),
    semesterId: this.fb.control<number | null>(null),
    value: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
    maxValue: this.fb.control<number | null>(null),
    comment: [''],
  });

  ngOnInit(): void {
    const u = this.auth.user();
    this.myName.set(`${u?.prenom ?? ''} ${u?.nom ?? ''}`.trim());
    forkJoin({
      c: this.api.myClasses(), s: this.api.mySubjects(),
      sem: this.api.semesters(), a: this.api.myAssessments(),
    }).subscribe({
      next: ({ c, s, sem, a }) => {
        this.classes.set(c); this.subjects.set(s); this.semesters.set(sem); this.assessments.set(a);
      },
    });
    this.load();
  }

  /** A teacher may only edit grades they recorded — the backend enforces it too. */
  isMine(g: GradeResponse): boolean {
    return !!g.teacherName && g.teacherName === this.myName();
  }

  onEntryClass(id: number | null): void {
    this.entryClasseId.set(id);
    this.entryStudents.set([]);
    this.form.patchValue({ studentId: null });
    if (id !== null) {
      this.api.studentsInClass(id).subscribe({ next: (p) => this.entryStudents.set(p.content) });
    }
  }

  load(): void {
    this.loading.set(true);
    this.api.searchGrades({
      classeId: this.filterClasseId(), subjectId: this.filterSubjectId(),
      semesterId: this.filterSemesterId(), page: this.page(), size: 10,
    }).subscribe({
      next: (p) => {
        this.items.set(p.content); this.totalPages.set(p.totalPages);
        this.total.set(p.totalElements); this.loading.set(false);
      },
      error: () => { this.error.set('Could not load grades.'); this.loading.set(false); },
    });
  }

  go(page: number): void { this.page.set(page); this.load(); }

  edit(g: GradeResponse): void {
    this.editingId.set(g.id);
    this.form.patchValue({
      studentId: g.student.id, assessmentId: g.assessmentId ?? null,
      subjectId: g.subject.id, semesterId: g.semesterId ?? null,
      value: g.value, maxValue: g.maxValue, comment: g.comment ?? '',
    });
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.form.reset({ studentId: null, assessmentId: null, subjectId: null, semesterId: null, value: null, maxValue: null, comment: '' });
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.formError.set(null);
    const raw = this.form.getRawValue();
    // Strip empties so the backend applies its assessment-inheritance rules.
    const body: Record<string, unknown> = { studentId: raw.studentId, value: raw.value };
    if (raw.assessmentId) body['assessmentId'] = raw.assessmentId;
    if (raw.subjectId) body['subjectId'] = raw.subjectId;
    if (raw.semesterId) body['semesterId'] = raw.semesterId;
    if (raw.maxValue) body['maxValue'] = raw.maxValue;
    if (raw.comment) body['comment'] = raw.comment;

    const id = this.editingId();
    const req = id ? this.api.updateGrade(id, body) : this.api.createGrade(body);
    req.subscribe({
      next: () => { this.saving.set(false); this.cancelEdit(); this.load(); },
      error: (e) => { this.saving.set(false); this.formError.set(e?.error?.message ?? 'Could not save the grade.'); },
    });
  }

  remove(g: GradeResponse): void {
    if (!confirm(`Delete this grade for ${g.student.prenom} ${g.student.nom}?`)) return;
    this.api.deleteGrade(g.id).subscribe({
      next: () => { this.error.set(null); this.load(); },
      error: (e) => this.error.set(e?.error?.message ?? 'Could not delete the grade.'),
    });
  }
}
