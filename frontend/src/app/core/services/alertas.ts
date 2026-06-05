import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Alerta {
  tipo: string;
  titulo: string;
  detalle: string;
  severidad: 'CRITICA' | 'ALTA' | 'MEDIA' | 'BAJA';
  fecha: string;
}

@Injectable({ providedIn: 'root' })
export class AlertasService {
  private apiUrl = `${environment.apiUrl}/alertas`;

  constructor(private http: HttpClient) {}

  list(): Observable<Alerta[]> {
    return this.http.get<Alerta[]>(this.apiUrl);
  }
}
