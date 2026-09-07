import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { AcademicService } from '../../core/services/academic.service';
import { StudentResponse } from '../../core/models/domain.models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h1>Admin dashboard</h1>

    @if (loading()) {
      <p class="muted">Loading…</p>
    } @else if (error()) {
      <div class="alert">{{ error() }}</div>
    } @else {
      <div class="stats">
        <div class="stat">
          <span class="num">{{ students().length }}</span>
          <span class="label">Students</span>
        </div>
      </div>

      <h2>Students</h2>
      @if (students().length === 0) {
        <p class="muted">No students yet.</p>
      } @else {
        <table>
          <thead>
            <tr><th>ID</th><th>Last name</th><th>First name</th><th>Age</th><th>Class</th></tr>
          </thead>
          <tbody>
            @for (s of students(); track s.id) {
              <tr>
                <td>{{ s.id }}</td>
                <td>{{ s.nom }}</td>
                <td>{{ s.prenom }}</td>
                <td>{{ s.age }}</td>
                <td>{{ s.classe ? ('Room ' + s.classe.salle) : '—' }}</td>
              </tr>
            }
          </tbody>
        </table>
      }
    }
  `,
})
export class AdminDashboardComponent implements OnInit {
  private readonly academic = inject(AcademicService);

  readonly students = signal<StudentResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.academic.getStudents().subscribe({
      next: (list) => {
        this.students.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load students.');
        this.loading.set(false);
      },
    });
  }
}
