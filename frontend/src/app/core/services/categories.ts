import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Categoria {
  id: number;
  nombre: string;
  codigo: string;
  descripcion: string;
  imagen: string;
  estado: number;
}

export interface CategoriaPayload {
  nombre: string;
  codigo: string;
  descripcion: string;
  imagen: string;
  estado: number;
}

@Injectable({ providedIn: 'root' })
export class CategoriesService {
  private apiUrl = `${environment.apiUrl}/categorias`;

  constructor(private http: HttpClient) {}

  list(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.apiUrl);
  }

  create(payload: CategoriaPayload): Observable<Categoria> {
    return this.http.post<Categoria>(this.apiUrl, payload);
  }

  update(id: number, payload: CategoriaPayload): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
