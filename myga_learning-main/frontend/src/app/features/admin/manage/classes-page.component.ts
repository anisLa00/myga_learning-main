import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { ClasseResponse } from '../../../core/models/domain.models';

@Component({
  selector: 'app-manage-classes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h1>Classes</h1>

    <form class="panel" [formGroup]="form" (ngSubmit)="save()">
      <h2>{{ editingId() ? 'Edit class' : 'Add class' }}</h2>
      <div class="row">
        <label>Room number<input type="number" formControlName="salle" /></label>
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
      <p class="muted">No classes yet.</p>
    } @else {
      <table>
        <thead><tr><th>Class</th><th>Students</th><th></th></tr></thead>
        <tbody>
          @for (c of items(); track c.id) {
            <tr>
              <td>Room {{ c.salle }}</td>
              <td>{{ c.students.length }}</td>
              <td class="right">
                <button class="ghost" (click)="edit(c)">Edit</button>
                <button class="danger" (click)="remove(c)">Delete</button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
})
export class ManageClassesComponent implements OnInit {
  private readonly api = inject(AdminService);
  private readonly fb = inject(FormBuilder);

  readonly items = signal<ClasseResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly formError = signal<string | null>(null);
  readonly saving = signal(false);
  readonly editingId = signal<number | null>(null);

  readonly form = this.fb.nonNullable.group({
    salle: [1, [Validators.required, Validators.min(1)]],
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.api.classes().subscribe({
      next: (c) => { this.items.set(c); this.loading.set(false); },
      error: () => { this.error.set('Could not load classes.'); this.loading.set(false); },
    });
  }

  edit(c: ClasseResponse): void { this.editingId.set(c.id); this.form.patchValue({ salle: c.salle }); }
  cancelEdit(): void { this.editingId.set(null); this.form.reset({ salle: 1 }); }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true); this.formError.set(null);
    const id = this.editingId();
    const req = id ? this.api.updateClasse(id, this.form.getRawValue()) : this.api.createClasse(this.form.getRawValue());
    req.subscribe({
      next: () => { this.saving.set(false); this.cancelEdit(); this.load(); },
      error: (e) => { this.saving.set(false); this.formError.set(e?.error?.message ?? 'Could not save the class.'); },
    });
  }

  remove(c: ClasseResponse): void {
    if (!confirm(`Delete room ${c.salle}?`)) return;
    this.api.deleteClasse(c.id).subscribe({
      next: () => { this.error.set(null); this.load(); },
      // A class still holding students/records returns 409 from the backend.
      error: (e) => this.error.set(e?.error?.message ?? 'Could not delete the class.'),
    });
  }
}
