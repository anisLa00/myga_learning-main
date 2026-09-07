import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models/auth.models';
import { AuthService } from '../services/auth.service';

/** Blocks a route unless the user is authenticated. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated()) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};

/** Blocks a route unless the user holds one of the allowed roles. */
export const roleGuard = (allowed: Role[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const role = auth.role;
    if (role && allowed.includes(role)) {
      return true;
    }
    router.navigate([auth.homePathForRole(role)]);
    return false;
  };
};
