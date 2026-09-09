import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { TeacherService } from '../../../core/services/teacher.service';
import {
  ClasseSummary, ObservationResponse, ObservationType, StudentResponse, SubjectResponse,
} from '../../../core/models/domain.models';

const TYPES: ObservationType[] =
  ['ACADEMIC', 'BEHAVIOR', 'PARTICIPATION', 'HOMEWORK', 'POSITIVE_FEEDBACK', 'CONCERN'];

@Component({
  selector: 'app-teacher-observations',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <h1>Observations</h1>
    <p class="muted small">
      Feedback marked visible reaches the parents and notifies them. Unmark it to keep a private,
      staff-only note.
    </p>

    <form class="panel" [formGroup]="form" (ngSubmit)="save()">
      <h2>Record an observation</h2>
      <div class="row">
        <label>Class
          <select [ngModel]="classeId()" (ngModelChange)="onClass($event)" [ngModelOptions]="{standalone: true}">
            <option [ngValue]="null">— choose —</option>
            @for (c of classes(); track c.id) { <option [ngValue]="c.id">Room {{ c.salle }}</option> }
          </select>
        </label>
        <label>Student
          <select formControlName="studentId">
            <option [ngValue]="null">— choose —</option>
            @for (s of students(); track s.id) { <option [ngValue]="s.id">{{ s.prenom }} {{ s.nom }}</option> }
          </select>
        </label>
        <label>Subject (optional)
          <select formControlName="subjectId">
            <option [ngValue]="null">— none —</option>
            @for (s of subjects(); track s.id) { <option [ngValue]="s.id">{{ s.nom }}</option> }
          </select>
        </label>
        <label>Type
          <select formControlName="type">
            @for (t of types; track t) { <option [ngValue]="t">{{ t }}</option> }
          </select>
        </label>
      </div>
      <div class="row">
        <label style="flex:1">Message<input formControlName="message" placeholder="What did you observe?" /></label>
      </div>
      <label class="check">
        <input type="checkbox" formControlName="visibleToParents" />
        Visible to parents
      </label>
      @if (formError()) { <div class="alert">{{ formError() }}</div> }
      @if (message()) { <div class="notice">{{ message() }}</div> }
      <div class="actions"><button type="submit" [disabled]="saving()">{{ saving() ? 'Saving…' : 'Record' }}</button></div>
    </form>

    @if (form.controls.studentId.value) {
      <h2>Observations for the selected student</h2>
      @if (historyLoading()) {
        <p class="muted">Loading…</p>
      } @else if (history().length === 0) {
        <p class="muted">Nothing recorded yet.</p>
      } @else {
        <ul class="feed">
          @for (o of history(); track o.id) {
            <li>
              <strong>{{ o.type }}</strong>
              @if (!o.visibleToParents) { <span class="tag private">private</span> }
              @else { <span class="tag ok">shared with parents</span> }
              <p class="muted">{{ o.message }}</p>
              <span class="muted small">{{ o.teacherName }} · {{ o.date }}</span>
            </li>
          }
        </ul>
      }
    }
  `,
})
export class TeacherObservationsComponent implements OnInit {
  private readonly api = inject(TeacherService);
  private readonly fb = inject(FormBuilder);
  readonly types = TYPES;

  readonly classes = signal<ClasseSummary[]>([]);
  readonly subjects = signal<SubjectResponse[]>([]);
  readonly students = signal<StudentResponse[]>([]);
  readonly history = signal<ObservationResponse[]>([]);
  readonly classeId = signal<number | null>(null);

  readonly saving = signal(false);
  readonly historyLoading = signal(false);
  readonly formError = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    studentId: this.fb.control<number | null>(null, Validators.required),
    subjectId: this.fb.control<number | null>(null),
    type: this.fb.control<ObservationType>('ACADEMIC', Validators.required),
    message: ['', Validators.required],
    visibleToParents: [true],
  });

  ngOnInit(): void {
    forkJoin({ c: this.api.myClasses(), s: this.api.mySubjects() }).subscribe({
      next: ({ c, s }) => { this.classes.set(c); this.subjects.set(s); },
    });
    // Reload the history whenever a different student is picked.
    this.form.controls.studentId.valueChanges.subscribe((id) => this.loadHistory(id));
  }

  onClass(id: number | null): void {
    this.classeId.set(id);
    this.students.set([]);
    this.form.patchValue({ studentId: null });
    if (id !== null) this.api.studentsInClass(id).subscribe({ next: (p) => this.students.set(p.content) });
  }

  private loadHistory(studentId: number | null): void {
    if (studentId === null) { this.history.set([]); return; }
    this.historyLoading.set(true);
    this.api.studentObservations(studentId).subscribe({
      next: (o) => { this.history.set(o); this.historyLoading.set(false); },
      error: () => { this.history.set([]); this.historyLoading.set(false); },
    });
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.formError.set(null); this.message.set(null);
    const raw = this.form.getRawValue();
    this.api.createObservation({
      studentId: raw.studentId!, subjectId: raw.subjectId, type: raw.type!,
      message: raw.message, visibleToParents: raw.visibleToParents,
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.message.set(raw.visibleToParents
          ? 'Observation recorded — the parents have been notified.'
          : 'Private observation recorded (not visible to parents).');
        this.form.patchValue({ message: '' });
        this.loadHistory(raw.studentId);
      },
      error: (e) => { this.saving.set(false); this.formError.set(e?.error?.message ?? 'Could not record the observation.'); },
    });
  }
}
