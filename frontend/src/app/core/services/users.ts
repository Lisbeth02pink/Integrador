import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface User {
  id: number;
  nombre: string;
  usuario: string;
  correo: string;
  estado: number;
  perfilId: number | null;
  perfilNombre: string | null;
}

export interface CreateUserPayload {
  nombre: string;
  usuario: string;
  clave: string;
  correo: string;
  idPerfil: number;
}

export interface UpdateUserPayload {
  nombre: string;
  usuario: string;
  clave: string;
  correo: string;
  idPerfil: number;
  estado?: number; 
}

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  private apiUrl = `${environment.apiUrl}/usuarios`;

  constructor(private http: HttpClient) {}

  list(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  create(payload: CreateUserPayload): Observable<User> {
    return this.http.post<User>(this.apiUrl, payload);
  }

  update(userId: number, payload: UpdateUserPayload): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${userId}`, payload);
  }

  toggleStatus(userId: number): Observable<User> {
    return this.http.patch<User>(`${this.apiUrl}/${userId}/estado`, {});
  }

  delete(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}`);
  }
}
