import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Attaches the JWT as a Bearer token to every outgoing request and, on a 401,
 * clears the session and redirects to the login page.
 *
 * Two endpoints are exempt from that: on them a 401 means "the password you
 * just typed is wrong", not "your session has expired", and signing the user
 * out would be both wrong and confusing.
 */
const CREDENTIAL_CHECKS = ['/auth/login', '/auth/change-password'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const token = auth.token;
  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  const checksTypedPassword = CREDENTIAL_CHECKS.some((path) => req.url.includes(path));

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !checksTypedPassword) {
        auth.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
