import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { SubjectResponse } from '../../../core/models/domain.models';

@Component({
  selector: 'app-manage-subjects',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h1>Subjects</h1>

    <form class="panel" [formGroup]="form" (ngSubmit)="save()">
      <h2>{{ editingId() ? 'Edit subject' : 'Add subject' }}</h2>
      <div class="row">
        <label>Name<input formControlName="nom" /></label>
        <label>Code<input formControlName="code" placeholder="MATH" /></label>
      </div>
      @if (formError()) { <div class="alert">{{ formError() }}</div> }
      <div class="actions">
        <button type="submit" [disabled]="saving()">{{ editingId() ? 'Update' : 'Create' }}</button>
        @if (editingId()) { <button type="button" class="ghost" (click)="cancelEdit()">Cancel</button> }
      </div>
    </form>

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (error()) {
      <div class="alert">{{ error() }}</div>
    } @else if (items().length === 0) {
      <p class="muted">No subjects yet.</p>
    } @else {
      <table>
        <thead><tr><th>Subject</th><th>Code</th><th></th></tr></thead>
        <tbody>
          @for (s of items(); track s.id) {
            <tr>
              <td>{{ s.nom }}</td>
              <td>{{ s.code || '—' }}</td>
              <td class="right">
                <button class="ghost" (click)="edit(s)">Edit</button>
                <button class="danger" (click)="remove(s)">Delete</button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class ManageSubjectsComponent implements OnInit {
  private readonly api = inject(AdminService);
  private readonly fb = inject(FormBuilder);

  readonly items = signal<SubjectResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly formError = signal<string | null>(null);
  readonly saving = signal(false);
  readonly editingId = signal<number | null>(null);

  readonly form = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    code: [''],
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.api.subjects().subscribe({
      next: (s) => { this.items.set(s); this.loading.set(false); },
      error: () => { this.error.set('Could not load subjects.'); this.loading.set(false); },
    });
  }

  edit(s: SubjectResponse): void { this.editingId.set(s.id); this.form.patchValue({ nom: s.nom, code: s.code ?? '' }); }
  cancelEdit(): void { this.editingId.set(null); this.form.reset({ nom: '', code: '' }); }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.formError.set(null);
    const id = this.editingId();
    const req = id ? this.api.updateSubject(id, this.form.getRawValue()) : this.api.createSubject(this.form.getRawValue());
    req.subscribe({
      next: () => { this.saving.set(false); this.cancelEdit(); this.load(); },
      error: (e) => { this.saving.set(false); this.formError.set(e?.error?.message ?? 'Could not save the subject.'); },
    });
  }

  remove(s: SubjectResponse): void {
    if (!confirm(`Delete ${s.nom}?`)) return;
    this.api.deleteSubject(s.id).subscribe({
      next: () => { this.error.set(null); this.load(); },
      error: (e) => this.error.set(e?.error?.message ?? 'Could not delete the subject.'),
    });
  }
}
