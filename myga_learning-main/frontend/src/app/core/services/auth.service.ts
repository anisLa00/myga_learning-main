import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthUser, LoginRequest, LoginResponse, Role } from '../models/auth.models';

const TOKEN_KEY = 'myga.token';
const USER_KEY = 'myga.user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly _user = signal<AuthUser | null>(this.readStoredUser());
  /** The currently authenticated user, or null. */
  readonly user = this._user.asReadonly();
  readonly isAuthenticated = computed(() => this._user() !== null);

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, payload).pipe(
      tap((res) => {
        this.setToken(res.accessToken);
        const user: AuthUser = { email: res.email, role: res.role, nom: res.nom, prenom: res.prenom };
        this.setStoredUser(user);
        this._user.set(user);
      })
    );
  }

  logout(): void {
    this.remove(TOKEN_KEY);
    this.remove(USER_KEY);
    this._user.set(null);
  }

  get token(): string | null {
    return this.read(TOKEN_KEY);
  }

  get role(): Role | null {
    return this._user()?.role ?? null;
  }

  homePathForRole(role: Role | null): string {
    switch (role) {
      case 'ADMIN':
        return '/admin';
      case 'TEACHER':
        return '/teacher';
      case 'PARENT':
        return '/parent';
      default:
        return '/login';
    }
  }

  // --- storage helpers (guarded so the app still renders if storage is unavailable) ---

  private setToken(token: string): void {
    this.write(TOKEN_KEY, token);
  }

  private setStoredUser(user: AuthUser): void {
    this.write(USER_KEY, JSON.stringify(user));
  }

  private readStoredUser(): AuthUser | null {
    const raw = this.read(USER_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as AuthUser;
    } catch {
      return null;
    }
  }

  private read(key: string): string | null {
    try {
      return localStorage.getItem(key);
    } catch {
      return null;
    }
  }

  private write(key: string, value: string): void {
    try {
      localStorage.setItem(key, value);
    } catch {
      /* ignore */
    }
  }

  private remove(key: string): void {
    try {
      localStorage.removeItem(key);
    } catch {
      /* ignore */
    }
  }
}
