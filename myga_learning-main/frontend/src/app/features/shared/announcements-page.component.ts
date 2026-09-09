import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AnnouncementService } from '../../core/services/announcement.service';
import { AdminService } from '../../core/services/admin.service';
import { AuthService } from '../../core/services/auth.service';
import { AnnouncementResponse } from '../../core/models/dashboard.models';
import { ClasseResponse } from '../../core/models/domain.models';

type Target = 'ALL' | 'PARENTS' | 'TEACHERS' | 'CLASS';

@Component({
  selector: 'app-announcements',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <h1>Announcements</h1>

    @if (isAdmin()) {
      <form class="panel" [formGroup]="form" (ngSubmit)="publish()">
        <h2>Publish an announcement</h2>
        <p class="muted small">Everyone targeted receives an in-app notification straight away.</p>
        <div class="row">
          <label style="flex:1">Title<input formControlName="title" /></label>
          <label>Audience
            <select formControlName="target">
              <option value="ALL">Everyone</option>
              <option value="PARENTS">All parents</option>
              <option value="TEACHERS">All teachers</option>
              <option value="CLASS">A specific class</option>
            </select>
          </label>
          @if (form.controls.target.value === 'CLASS') {
            <label>Class
              <select formControlName="classeId">
                <option [ngValue]="null">— choose —</option>
                @for (c of classes(); track c.id) { <option [ngValue]="c.id">Room {{ c.salle }}</option> }
              </select>
            </label>
          }
        </div>
        <div class="row">
          <label style="flex:1">Message<input formControlName="message" /></label>
        </div>
        @if (formError()) { <div class="alert">{{ formError() }}</div> }
        @if (message()) { <div class="notice">{{ message() }}</div> }
        <div class="actions"><button type="submit" [disabled]="saving()">{{ saving() ? 'Publishing…' : 'Publish' }}</button></div>
      </form>
    }

    <h2>{{ isAdmin() ? 'All announcements' : 'For you' }}</h2>
    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (error()) {
      <div class="alert">{{ error() }}</div>
    } @else if (items().length === 0) {
      <p class="muted">Nothing published yet.</p>
    } @else {
      <ul class="feed">
        @for (a of items(); track a.id) {
          <li>
            <strong>{{ a.title }}</strong>
            <span class="tag">{{ a.target }}</span>
            <p class="muted">{{ a.message }}</p>
            <span class="muted small">
              {{ a.createdAt }} @if (a.createdByEmail) { · {{ a.createdByEmail }} }
            </span>
          </li>
        }
      </ul>
    }
  `,
})
export class AnnouncementsComponent implements OnInit {
  private readonly api = inject(AnnouncementService);
  private readonly admin = inject(AdminService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly items = signal<AnnouncementResponse[]>([]);
  readonly classes = signal<ClasseResponse[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly formError = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    message: ['', Validators.required],
    target: this.fb.control<Target>('ALL', Validators.required),
    classeId: this.fb.control<number | null>(null),
  });

  isAdmin(): boolean { return this.auth.user()?.role === 'ADMIN'; }

  ngOnInit(): void {
    if (this.isAdmin()) {
      this.admin.classes().subscribe({ next: (c) => this.classes.set(c) });
    }
    this.load();
  }

  load(): void {
    this.loading.set(true);
    // Admins get the management view; everyone else sees what concerns them.
    const req = this.isAdmin() ? this.api.all() : this.api.mine();
    req.subscribe({
      next: (a) => { this.items.set(a); this.loading.set(false); },
      error: () => { this.error.set('Could not load announcements.'); this.loading.set(false); },
    });
  }

  publish(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const raw = this.form.getRawValue();
    if (raw.target === 'CLASS' && !raw.classeId) {
      this.formError.set('Choose a class for a class announcement.');
      return;
    }
    this.saving.set(true); this.formError.set(null); this.message.set(null);
    this.api.create({
      title: raw.title, message: raw.message, target: raw.target!,
      classeId: raw.target === 'CLASS' ? raw.classeId : null,
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.message.set('Published — the targeted users have been notified.');
        this.form.patchValue({ title: '', message: '' });
        this.load();
      },
      error: (e) => { this.saving.set(false); this.formError.set(e?.error?.message ?? 'Could not publish.'); },
    });
  }
}
