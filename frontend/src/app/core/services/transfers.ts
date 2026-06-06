import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TransferenciaRecepcion {
  id: number;
  almacenOrigenNombre: string;
  almacenDestinoNombre: string;
  estado: string;
  fechaCreacion: string;
  productoNombre: string;
  productoId: number;
  cantidad: number;
}

@Injectable({
  providedIn: 'root'
})
export class TransfersService {
  private apiUrl = 'http://localhost:8080/api/transferencias';

  constructor(private http: HttpClient) {}

  list(): Observable<TransferenciaRecepcion[]> {
    return this.http.get<TransferenciaRecepcion[]>(this.apiUrl);
  }
}
