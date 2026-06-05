import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Warehouse {
  id: number;
  nombre: string;
  ciudad: string;
  responsable: string;
  direccion: string;
  tipo: 'CENTRAL' | 'REGIONAL' | 'TIENDA';
  capacidad: number;
  ocupacion: number;
  estado: number;
}

export interface WarehousePayload {
  nombre: string;
  ciudad: string;
  responsable: string;
  direccion: string;
  tipo: 'CENTRAL' | 'REGIONAL' | 'TIENDA';
  capacidad: number;
  ocupacion: number;
  estado: number;
}

@Injectable({ providedIn: 'root' })
export class WarehousesService {
  private apiUrl = `${environment.apiUrl}/almacenes`;

  constructor(private http: HttpClient) {}

  list(): Observable<Warehouse[]> {
    return this.http.get<Warehouse[]>(this.apiUrl);
  }

  create(payload: WarehousePayload): Observable<Warehouse> {
    return this.http.post<Warehouse>(this.apiUrl, payload);
  }

  update(id: number, payload: WarehousePayload): Observable<Warehouse> {
    return this.http.put<Warehouse>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
