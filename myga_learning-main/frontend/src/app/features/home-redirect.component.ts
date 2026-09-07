import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

/** Landing route that forwards the user to the dashboard matching their role. */
@Component({
  selector: 'app-home-redirect',
  standalone: true,
  template: `<p>Redirecting…</p>`,
})
export class HomeRedirectComponent {
  constructor() {
    const auth = inject(AuthService);
    const router = inject(Router);
    router.navigate([auth.homePathForRole(auth.role)]);
  }
}
