import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { ClasseResponse, StudentResponse } from '../../../core/models/domain.models';

@Component({
  selector: 'app-manage-students',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h1>Students</h1>

    <form class="panel" [formGroup]="form" (ngSubmit)="save()">
      <h2>{{ editingId() ? 'Edit student' : 'Add student' }}</h2>
      <div class="row">
        <label>Last name<input formControlName="nom" /></label>
        <label>First name<input formControlName="prenom" /></label>
        <label>Age<input type="number" formControlName="age" /></label>
        <label>Class
          <select formControlName="classeId">
            <option [ngValue]="null">— none —</option>
            @for (c of classes(); track c.id) { <option [ngValue]="c.id">Room {{ c.salle }}</option> }
          </select>
        </label>
      </div>
      @if (formError()) { <div class="alert">{{ formError() }}</div> }
      <div class="actions">
        <button type="submit" [disabled]="saving()">{{ saving() ? 'Saving…' : (editingId() ? 'Update' : 'Create') }}</button>
        @if (editingId()) { <button type="button" class="ghost" (click)="cancelEdit()">Cancel</button> }
      </div>
    </form>

    <div class="toolbar">
      <input placeholder="Search by name…" [value]="search()" (input)="onSearch($event)" />
      <span class="spacer"></span>
      <span class="muted">{{ total() }} student(s)</span>
    </div>

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (error()) {
      <div class="alert">{{ error() }}</div>
    } @else if (items().length === 0) {
      <p class="muted">No students found.</p>
    } @else {
      <table>
        <thead><tr><th>Name</th><th>Age</th><th>Class</th><th></th></tr></thead>
        <tbody>
          @for (s of items(); track s.id) {
            <tr>
              <td>{{ s.prenom }} {{ s.nom }}</td>
              <td>{{ s.age }}</td>
              <td>{{ s.classe ? 'Room ' + s.classe.salle : '—' }}</td>
              <td class="right">
                <button class="ghost" (click)="edit(s)">Edit</button>
                <button class="danger" (click)="remove(s)">Delete</button>
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
export class ManageStudentsComponent implements OnInit {
  private readonly api = inject(AdminService);
  private readonly fb = inject(FormBuilder);

  readonly items = signal<StudentResponse[]>([]);
  readonly classes = signal<ClasseResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly formError = signal<string | null>(null);
  readonly saving = signal(false);
  readonly editingId = signal<number | null>(null);

  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly total = signal(0);
  readonly search = signal('');
  private searchTimer?: ReturnType<typeof setTimeout>;

  readonly form = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    age: [10, [Validators.required, Validators.min(1)]],
    classeId: this.fb.control<number | null>(null),
  });

  ngOnInit(): void {
    this.api.classes().subscribe({ next: (c) => this.classes.set(c) });
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api.students({ search: this.search(), page: this.page(), size: 10 }).subscribe({
      next: (p) => {
        this.items.set(p.content);
        this.totalPages.set(p.totalPages);
        this.total.set(p.totalElements);
        this.loading.set(false);
      },
      error: () => { this.error.set('Could not load students.'); this.loading.set(false); },
    });
  }

  onSearch(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
    // Debounce so we do not fire a request per keystroke.
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => { this.page.set(0); this.load(); }, 300);
  }

  go(page: number): void { this.page.set(page); this.load(); }

  edit(s: StudentResponse): void {
    this.editingId.set(s.id);
    this.form.patchValue({ nom: s.nom, prenom: s.prenom, age: s.age, classeId: s.classe?.id ?? null });
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.form.reset({ nom: '', prenom: '', age: 10, classeId: null });
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    this.formError.set(null);
    const body = this.form.getRawValue();
    const id = this.editingId();
    const req = id ? this.api.updateStudent(id, body) : this.api.createStudent(body);
    req.subscribe({
      next: () => { this.saving.set(false); this.cancelEdit(); this.load(); },
      error: (e) => { this.saving.set(false); this.formError.set(e?.error?.message ?? 'Could not save the student.'); },
    });
  }

  remove(s: StudentResponse): void {
    if (!confirm(`Delete ${s.prenom} ${s.nom}?`)) return;
    this.api.deleteStudent(s.id).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(e?.error?.message ?? 'Could not delete the student.'),
    });
  }
}
