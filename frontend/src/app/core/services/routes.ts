import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { InventoryTransfer } from './inventory';

export type DeliveryRouteStatus =
  | 'pendiente'
  | 'preparado'
  | 'cargando'
  | 'en ruta'
  | 'detenido'
  | 'retrasado'
  | 'entregado'
  | 'cancelado';

export interface DeliveryRoute {
  id: number;
  nombre: string;
  zona: string;
  repartidor: string;
  pedidoId: number | null;
  transferenciaId: number | null;
  vehiculo?: string | null;
  tipoVehiculo?: string | null;
  placa?: string | null;
  capacidadVehiculo?: number | null;
  cantidadCarga?: number | null;
  origen?: string | null;
  destino?: string | null;
  fechaEntrega?: string | null;
  pedidos: number;
  estado: DeliveryRouteStatus;
  horaSalida: string;
  horaEstimadaLlegada?: string | null;
  horaEntregaReal?: string | null;
  ubicacionActual?: string | null;
  observaciones?: string | null;
  incidencias?: string | null;
  estadoGps?: string | null;
  evidenciaEntrega?: string | null;
  firmaDigital?: string | null;
  fotoEntrega?: string | null;
  vehiculoActivo?: boolean | null;
  conductorBloqueado?: boolean | null;
  confirmacionEntrega?: boolean | null;
  progreso: number;
}

export interface DeliveryRoutePayload {
  nombre: string;
  zona: string;
  repartidor: string;
  pedidoId?: number | null;
  transferenciaId?: number | null;
  vehiculo?: string | null;
  tipoVehiculo?: string | null;
  placa?: string | null;
  capacidadVehiculo?: number | null;
  cantidadCarga?: number | null;
  origen?: string | null;
  destino?: string | null;
  fechaEntrega?: string | null;
  pedidos: number;
  estado: DeliveryRouteStatus;
  horaSalida: string;
  horaEstimadaLlegada?: string | null;
  horaEntregaReal?: string | null;
  ubicacionActual?: string | null;
  observaciones?: string | null;
  incidencias?: string | null;
  estadoGps?: string | null;
  evidenciaEntrega?: string | null;
  firmaDigital?: string | null;
  fotoEntrega?: string | null;
  vehiculoActivo?: boolean | null;
  conductorBloqueado?: boolean | null;
  confirmacionEntrega?: boolean | null;
  progreso: number;
}

@Injectable({ providedIn: 'root' })
export class RoutesService {
  private apiUrl = `${environment.apiUrl}/rutas`;

  constructor(private http: HttpClient) {}

  list(): Observable<DeliveryRoute[]> {
    return this.http.get<DeliveryRoute[]>(this.apiUrl);
  }

  listAvailableTransfers(): Observable<InventoryTransfer[]> {
    return this.http.get<InventoryTransfer[]>(`${this.apiUrl}/transferencias-disponibles`);
  }

  create(payload: DeliveryRoutePayload): Observable<DeliveryRoute> {
    return this.http.post<DeliveryRoute>(this.apiUrl, payload);
  }

  update(id: number, payload: DeliveryRoutePayload): Observable<DeliveryRoute> {
    return this.http.put<DeliveryRoute>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
