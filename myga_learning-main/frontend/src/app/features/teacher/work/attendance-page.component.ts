import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TeacherService, BulkAttendanceEntry } from '../../../core/services/teacher.service';
import {
  AttendanceStatus, ClasseSummary, StudentResponse, SubjectResponse,
} from '../../../core/models/domain.models';

const STATUSES: AttendanceStatus[] = ['PRESENT', 'ABSENT', 'LATE', 'EXCUSED'];

@Component({
  selector: 'app-teacher-attendance',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <h1>Mark attendance</h1>
    <p class="muted small">Mark a whole class in one go. You can only mark classes assigned to you.</p>

    <div class="panel">
      <div class="row">
        <label>Class
          <select [ngModel]="classeId()" (ngModelChange)="onClassChange($event)">
            <option [ngValue]="null">— choose —</option>
            @for (c of classes(); track c.id) { <option [ngValue]="c.id">Room {{ c.salle }}</option> }
          </select>
        </label>
        <label>Date
          <input type="date" [ngModel]="date()" (ngModelChange)="onDateChange($event)" />
        </label>
        <label>Subject (optional)
          <select [ngModel]="subjectId()" (ngModelChange)="subjectId.set($event)">
            <option [ngValue]="null">— none —</option>
            @for (s of subjects(); track s.id) { <option [ngValue]="s.id">{{ s.nom }}</option> }
          </select>
        </label>
      </div>
    </div>

    @if (message()) { <div class="notice">{{ message() }}</div> }
    @if (error()) { <div class="alert">{{ error() }}</div> }

    @if (classeId() === null) {
      <p class="muted">Choose one of your classes to begin.</p>
    } @else if (loading()) {
      <p class="muted">Loading students…</p>
    } @else if (students().length === 0) {
      <p class="muted">This class has no students yet.</p>
    } @else {
      @if (alreadyMarked().length > 0) {
        <div class="notice">
          Attendance was already recorded for this date ({{ alreadyMarked().length }} entr(y/ies)).
          Submitting again adds new records.
        </div>
      }
      <div class="toolbar">
        <span class="muted small">Set all:</span>
        @for (s of statuses; track s) {
          <button class="ghost" (click)="setAll(s)">{{ s }}</button>
        }
      </div>
      <table>
        <thead><tr><th>Student</th><th>Status</th><th>Note</th></tr></thead>
        <tbody>
          @for (st of students(); track st.id) {
            <tr>
              <td>{{ st.prenom }} {{ st.nom }}</td>
              <td>
                <select [ngModel]="statusFor(st.id)" (ngModelChange)="setStatus(st.id, $event)">
                  @for (s of statuses; track s) { <option [ngValue]="s">{{ s }}</option> }
                </select>
              </td>
              <td>
                <input placeholder="optional note"
                       [ngModel]="noteFor(st.id)" (ngModelChange)="setNote(st.id, $event)" />
              </td>
            </tr>
          }
        </tbody>
      </table>
      <div class="actions">
        <button (click)="submit()" [disabled]="saving()">{{ saving() ? 'Saving…' : 'Save attendance' }}</button>
      </div>
    }
  `,
})
export class TeacherAttendanceComponent implements OnInit {
  private readonly api = inject(TeacherService);
  readonly statuses = STATUSES;

  readonly classes = signal<ClasseSummary[]>([]);
  readonly subjects = signal<SubjectResponse[]>([]);
  readonly students = signal<StudentResponse[]>([]);
  readonly alreadyMarked = signal<{ id: number }[]>([]);

  readonly classeId = signal<number | null>(null);
  readonly subjectId = signal<number | null>(null);
  readonly date = signal<string>(new Date().toISOString().slice(0, 10));

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  /** studentId -> status / note, kept as plain maps in signals. */
  private readonly statuses$ = signal<Record<number, AttendanceStatus>>({});
  private readonly notes$ = signal<Record<number, string>>({});

  ngOnInit(): void {
    this.api.myClasses().subscribe({ next: (c) => this.classes.set(c) });
    this.api.mySubjects().subscribe({ next: (s) => this.subjects.set(s) });
  }

  onClassChange(id: number | null): void {
    this.classeId.set(id);
    this.message.set(null);
    if (id !== null) this.loadStudents();
  }

  onDateChange(d: string): void {
    this.date.set(d);
    this.message.set(null);
    if (this.classeId() !== null) this.loadRegister();
  }

  private loadStudents(): void {
    const id = this.classeId();
    if (id === null) return;
    this.loading.set(true);
    this.error.set(null);
    this.api.studentsInClass(id).subscribe({
      next: (p) => {
        this.students.set(p.content);
        // Default everyone to PRESENT — the common case.
        const defaults: Record<number, AttendanceStatus> = {};
        p.content.forEach((s) => (defaults[s.id] = 'PRESENT'));
        this.statuses$.set(defaults);
        this.notes$.set({});
        this.loading.set(false);
        this.loadRegister();
      },
      error: () => { this.error.set('Could not load the students.'); this.loading.set(false); },
    });
  }

  private loadRegister(): void {
    const id = this.classeId();
    if (id === null) return;
    this.api.classRegister(id, this.date()).subscribe({
      next: (r) => this.alreadyMarked.set(r),
      error: () => this.alreadyMarked.set([]),
    });
  }

  statusFor(studentId: number): AttendanceStatus { return this.statuses$()[studentId] ?? 'PRESENT'; }
  noteFor(studentId: number): string { return this.notes$()[studentId] ?? ''; }
  setStatus(studentId: number, s: AttendanceStatus): void { this.statuses$.update((m) => ({ ...m, [studentId]: s })); }
  setNote(studentId: number, n: string): void { this.notes$.update((m) => ({ ...m, [studentId]: n })); }
  setAll(s: AttendanceStatus): void {
    const all: Record<number, AttendanceStatus> = {};
    this.students().forEach((st) => (all[st.id] = s));
    this.statuses$.set(all);
  }

  submit(): void {
    const classeId = this.classeId();
    if (classeId === null) return;
    const entries: BulkAttendanceEntry[] = this.students().map((s) => ({
      studentId: s.id,
      status: this.statusFor(s.id),
      note: this.noteFor(s.id) || null,
    }));
    this.saving.set(true);
    this.error.set(null);
    this.message.set(null);
    this.api.markClass({ classeId, subjectId: this.subjectId(), date: this.date(), entries }).subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.message.set(`Attendance saved for ${saved.length} student(s).`);
        this.loadRegister();
      },
      error: (e) => {
        this.saving.set(false);
        this.error.set(e?.error?.message ?? 'Could not save the attendance.');
      },
    });
  }
}
