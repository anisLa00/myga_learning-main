import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AdminService } from '../../../core/services/admin.service';
import { ClasseResponse, SubjectResponse, TeacherResponse } from '../../../core/models/domain.models';

@Component({
  selector: 'app-manage-teachers',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h1>Teachers</h1>

    <form class="panel" [formGroup]="form" (ngSubmit)="create()">
      <h2>Add teacher</h2>
      <p class="muted small">Creating a teacher also provisions their TEACHER login — there is no public sign-up.</p>
      <div class="row">
        <label>Last name<input formControlName="nom" /></label>
        <label>First name<input formControlName="prenom" /></label>
        <label>Email<input type="email" formControlName="email" /></label>
        <label>Password<input type="password" formControlName="password" /></label>
      </div>
      @if (formError()) { <div class="alert">{{ formError() }}</div> }
      <div class="actions"><button type="submit" [disabled]="saving()">{{ saving() ? 'Saving…' : 'Create' }}</button></div>
    </form>

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (error()) {
      <div class="alert">{{ error() }}</div>
    } @else if (items().length === 0) {
      <p class="muted">No teachers yet.</p>
    } @else {
      @for (t of items(); track t.id) {
        <div class="panel">
          <div class="head">
            <strong>{{ t.prenom }} {{ t.nom }}</strong>
            <span class="muted">{{ t.email }}</span>
            <span class="spacer"></span>
            <button class="danger" (click)="remove(t)">Delete</button>
          </div>

          <div class="assign">
            <span class="muted small">Subjects:</span>
            @if (t.subjects.length === 0) { <span class="muted small">none</span> }
            @for (s of t.subjects; track s.id) {
              <span class="chip">{{ s.nom }} <button class="x" (click)="unassignSubject(t, s.id)">×</button></span>
            }
            <select #subjectSel>
              <option value="">+ add subject</option>
              @for (s of subjects(); track s.id) { <option [value]="s.id">{{ s.nom }}</option> }
            </select>
            <button class="ghost" (click)="assignSubject(t, subjectSel.value); subjectSel.value=''">Assign</button>
          </div>

          <div class="assign">
            <span class="muted small">Classes:</span>
            @if (t.classes.length === 0) { <span class="muted small">none</span> }
            @for (c of t.classes; track c.id) {
              <span class="chip">Room {{ c.salle }} <button class="x" (click)="unassignClasse(t, c.id)">×</button></span>
            }
            <select #classeSel>
              <option value="">+ add class</option>
              @for (c of classes(); track c.id) { <option [value]="c.id">Room {{ c.salle }}</option> }
            </select>
            <button class="ghost" (click)="assignClasse(t, classeSel.value); classeSel.value=''">Assign</button>
          </div>
        </div>
      }
    }
  `,
})
export class ManageTeachersComponent implements OnInit {
  private readonly api = inject(AdminService);
  private readonly fb = inject(FormBuilder);

  readonly items = signal<TeacherResponse[]>([]);
  readonly subjects = signal<SubjectResponse[]>([]);
  readonly classes = signal<ClasseResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly formError = signal<string | null>(null);
  readonly saving = signal(false);

  readonly form = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    forkJoin({ t: this.api.teachers(), s: this.api.subjects(), c: this.api.classes() }).subscribe({
      next: ({ t, s, c }) => {
        this.items.set(t); this.subjects.set(s); this.classes.set(c); this.loading.set(false);
      },
      error: () => { this.error.set('Could not load teachers.'); this.loading.set(false); },
    });
  }

  create(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.formError.set(null);
    this.api.createTeacher(this.form.getRawValue()).subscribe({
      next: () => { this.saving.set(false); this.form.reset({ nom: '', prenom: '', email: '', password: '' }); this.load(); },
      error: (e) => { this.saving.set(false); this.formError.set(e?.error?.message ?? 'Could not create the teacher.'); },
    });
  }

  assignSubject(t: TeacherResponse, value: string): void {
    if (!value) return;
    this.api.assignSubject(t.id, Number(value)).subscribe({ next: () => this.load() });
  }
  unassignSubject(t: TeacherResponse, subjectId: number): void {
    this.api.unassignSubject(t.id, subjectId).subscribe({ next: () => this.load() });
  }
  assignClasse(t: TeacherResponse, value: string): void {
    if (!value) return;
    this.api.assignClasse(t.id, Number(value)).subscribe({ next: () => this.load() });
  }
  unassignClasse(t: TeacherResponse, classeId: number): void {
    this.api.unassignClasse(t.id, classeId).subscribe({ next: () => this.load() });
  }

  remove(t: TeacherResponse): void {
    if (!confirm(`Delete ${t.prenom} ${t.nom} and their login?`)) return;
    this.api.deleteTeacher(t.id).subscribe({
      next: () => { this.error.set(null); this.load(); },
      // 409 when the teacher still has grades/attendance history.
      error: (e) => this.error.set(e?.error?.message ?? 'Could not delete the teacher.'),
    });
  }
}
