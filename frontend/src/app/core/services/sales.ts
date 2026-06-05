import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SaleSummary {
  id: number;
  fecha: string;
  canal: string;
  ingresos: number;
  egresos: number;
  productoMasVendido: string;
}

@Injectable({ providedIn: 'root' })
export class SalesService {
  private apiUrl = `${environment.apiUrl}/ventas/resumen`;

  constructor(private http: HttpClient) {}

  list(fromDate: string, toDate: string): Observable<SaleSummary[]> {
    const params = new HttpParams().set('desde', fromDate).set('hasta', toDate);
    return this.http.get<SaleSummary[]>(this.apiUrl, { params });
  }
}
