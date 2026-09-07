export type Role = 'ADMIN' | 'TEACHER' | 'PARENT';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  email: string;
  role: Role;
  nom?: string;
  prenom?: string;
}

export interface AuthUser {
  email: string;
  role: Role;
  nom?: string;
  prenom?: string;
}
