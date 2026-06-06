import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TransferenciaRecepcion } from './transfers';

export type { TransferenciaRecepcion };

export interface RecepcionPayload {
  transferenciaId: number;
  responsable: string;
  observaciones: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReceptionService {
  private apiUrl = 'http://localhost:8080/api/recepcion-tienda';

  constructor(private http: HttpClient) {}

  confirmReception(payload: RecepcionPayload): Observable<any> {
    return this.http.post(this.apiUrl, payload);
  }
}
