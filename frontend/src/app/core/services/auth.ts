import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  id: number;
  usuario: string;
  correo: string;
  nombre: string;
  perfil: string | null;
  modulos: string[];
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private apiUrl = `${environment.apiUrl}/auth`;
  private readonly storageKey = 'user';

  constructor(private http: HttpClient) {}

  login(data: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, data);
  }

  refresh(refreshToken: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/refresh`, { refreshToken });
  }

  logout(refreshToken: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/logout`, { refreshToken });
  }

  saveSession(data: LoginResponse) {
    localStorage.setItem(this.storageKey, JSON.stringify(data));
  }

  getSession(): LoginResponse | null {
    const user = localStorage.getItem(this.storageKey);
    return user ? (JSON.parse(user) as LoginResponse) : null;
  }

  getAccessToken() {
    return this.getSession()?.accessToken ?? null;
  }

  getRefreshToken() {
    return this.getSession()?.refreshToken ?? null;
  }

  isSessionExpired() {
    const session = this.getSession();
    return !session || Date.now() >= session.expiresAt;
  }

  clearSession() {
    localStorage.removeItem(this.storageKey);
  }
}
