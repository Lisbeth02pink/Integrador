import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Merma {
  movimientoId: number;
  fecha: string;
  productoId: number;
  productoNombre: string;
  almacenNombre: string;
  cantidad: number;
  motivo: string;
  responsable: string;
  observaciones: string;
}

export interface MermaPayload {
  productoId: number;
  cantidad: number;
  motivo: string;
  responsable: string;
  observaciones: string;
}

@Injectable({ providedIn: 'root' })
export class MermasService {
  private apiUrl = `${environment.apiUrl}/mermas`;

  constructor(private http: HttpClient) {}

  list(): Observable<Merma[]> {
    return this.http.get<Merma[]>(this.apiUrl);
  }

  create(payload: MermaPayload): Observable<Merma> {
    return this.http.post<Merma>(this.apiUrl, payload);
  }
}
