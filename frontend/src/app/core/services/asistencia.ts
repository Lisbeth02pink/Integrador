import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AsistenciaPerfil {
  id: number;
  userId: number | null;
  codigo: string;
  usuario: string | null;
  nombre: string;
  descriptor: number[];
  creadoEn: string;
}

export interface AsistenciaRegistro {
  id: number;
  userId: number | null;
  codigo: string;
  usuario: string | null;
  nombre: string;
  coincidencia: number;
  fecha: string;
  tipo: 'ENTRADA' | 'SALIDA';
}

@Injectable({
  providedIn: 'root',
})
export class AsistenciaService {
  private apiUrl = `${environment.apiUrl}/asistencia`;

  constructor(private http: HttpClient) {}

  listProfiles(): Observable<AsistenciaPerfil[]> {
    return this.http.get<AsistenciaPerfil[]>(`${this.apiUrl}/perfiles`);
  }

  saveProfile(payload: { userId: number; descriptor: number[] }): Observable<AsistenciaPerfil> {
    return this.http.post<AsistenciaPerfil>(`${this.apiUrl}/perfiles`, payload);
  }

  deleteProfile(profileId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/perfiles/${profileId}`);
  }

  listLogs(): Observable<AsistenciaRegistro[]> {
    return this.http.get<AsistenciaRegistro[]>(`${this.apiUrl}/registros`);
  }

  saveLog(payload: {
    userId: number;
    coincidencia: number;
    tipo: 'ENTRADA' | 'SALIDA';
  }): Observable<AsistenciaRegistro> {
    return this.http.post<AsistenciaRegistro>(`${this.apiUrl}/registros`, payload);
  }
}
