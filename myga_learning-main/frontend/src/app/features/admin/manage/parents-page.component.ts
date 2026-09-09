import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AdminService } from '../../../core/services/admin.service';
import { ParentResponse, StudentResponse } from '../../../core/models/domain.models';

@Component({
  selector: 'app-manage-parents',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h1>Parents</h1>

    <form class="panel" [formGroup]="form" (ngSubmit)="create()">
      <h2>Add parent</h2>
      <p class="muted small">Leave the password blank for a record with no login; fill it in to provision a PARENT account.</p>
      <div class="row">
        <label>Last name<input formControlName="nom" /></label>
        <label>First name<input formControlName="prenom" /></label>
        <label>Email<input type="email" formControlName="email" /></label>
        <label>Password (optional)<input type="password" formControlName="password" /></label>
      </div>
      @if (formError()) { <div class="alert">{{ formError() }}</div> }
      <div class="actions"><button type="submit" [disabled]="saving()">{{ saving() ? 'Saving…' : 'Create' }}</button></div>
    </form>

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (error()) {
      <div class="alert">{{ error() }}</div>
    } @else if (items().length === 0) {
      <p class="muted">No parents yet.</p>
    } @else {
      @for (p of items(); track p.phone) {
        <div class="panel">
          <div class="head">
            <strong>{{ p.prenom }} {{ p.nom }}</strong>
            <span class="muted">{{ p.email }}</span>
            <span class="spacer"></span>
            <button class="danger" (click)="remove(p)">Delete</button>
          </div>
          <div class="assign">
            <span class="muted small">Children:</span>
            @if (p.students.length === 0) { <span class="muted small">none linked</span> }
            @for (s of p.students; track s.id) {
              <span class="chip">{{ s.prenom }} {{ s.nom }} <button class="x" (click)="unlink(p, s.id)">×</button></span>
            }
            <select #childSel>
              <option value="">+ link child</option>
              @for (s of students(); track s.id) { <option [value]="s.id">{{ s.prenom }} {{ s.nom }}</option> }
            </select>
            <button class="ghost" (click)="link(p, childSel.value); childSel.value=''">Link</button>
          </div>
        </div>
      }
    }
  `,
})
export class ManageParentsComponent implements OnInit {
  private readonly api = inject(AdminService);
  private readonly fb = inject(FormBuilder);

  readonly items = signal<ParentResponse[]>([]);
  readonly students = signal<StudentResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly formError = signal<string | null>(null);
  readonly saving = signal(false);

  readonly form = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: [''],
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    forkJoin({ p: this.api.parents(), s: this.api.students({ size: 200 }) }).subscribe({
      next: ({ p, s }) => { this.items.set(p); this.students.set(s.content); this.loading.set(false); },
      error: () => { this.error.set('Could not load parents.'); this.loading.set(false); },
    });
  }

  create(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.formError.set(null);
    const raw = this.form.getRawValue();
    // Omit an empty password so the backend creates a record with no login.
    const body = raw.password ? raw : { nom: raw.nom, prenom: raw.prenom, email: raw.email };
    this.api.createParent(body).subscribe({
      next: () => { this.saving.set(false); this.form.reset({ nom: '', prenom: '', email: '', password: '' }); this.load(); },
      error: (e) => { this.saving.set(false); this.formError.set(e?.error?.message ?? 'Could not create the parent.'); },
    });
  }

  link(p: ParentResponse, value: string): void {
    if (!value) return;
    this.api.linkChild(p.phone, Number(value)).subscribe({ next: () => this.load() });
  }
  unlink(p: ParentResponse, studentId: number): void {
    this.api.unlinkChild(p.phone, studentId).subscribe({ next: () => this.load() });
  }

  remove(p: ParentResponse): void {
    if (!confirm(`Delete ${p.prenom} ${p.nom}?`)) return;
    this.api.deleteParent(p.phone).subscribe({
      next: () => { this.error.set(null); this.load(); },
      error: (e) => this.error.set(e?.error?.message ?? 'Could not delete the parent.'),
    });
  }
}
