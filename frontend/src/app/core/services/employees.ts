import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface EmployeeControl {
  id: number;
  nombre: string;
  cargo: string;
  entrada: string;
  salida: string;
  tardanzas: number;
  faltas: number;
  asistencias: number;
  estado: 'Presente' | 'Tarde' | 'Falta' | 'Pendiente';
}

@Injectable({ providedIn: 'root' })
export class EmployeesService {
  private apiUrl = `${environment.apiUrl}/empleados/control`;

  constructor(private http: HttpClient) {}

  list(): Observable<EmployeeControl[]> {
    return this.http.get<EmployeeControl[]>(this.apiUrl);
  }
}
