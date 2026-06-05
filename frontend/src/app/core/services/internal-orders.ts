import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { InventoryMovement } from './inventory';

export type InternalOrderPriority = 'Alta' | 'Media' | 'Baja';
export type InternalOrderStatus = 'Pendiente' | 'Aprobado' | 'Preparando' | 'En ruta' | 'Entregado';

export interface InternalOrderItem {
  productId: number;
  productName: string;
  sku: string;
  quantity: number;
}

export interface InternalOrder {
  id: number;
  storeId: number;
  storeName: string;
  requestedBy: string;
  priority: InternalOrderPriority;
  requestedAt: string;
  status: InternalOrderStatus;
  notes: string;
  transferGenerated: boolean;
  items: InternalOrderItem[];
}

export interface InternalOrderPayload {
  tiendaId: number;
  solicitadoPor: string;
  prioridad: InternalOrderPriority;
  observaciones: string;
  items: Array<{
    productoId: number;
    cantidad: number;
  }>;
}

@Injectable({ providedIn: 'root' })
export class InternalOrdersService {
  private apiUrl = `${environment.apiUrl}/pedidos-internos`;

  constructor(private http: HttpClient) {}

  list(): Observable<InternalOrder[]> {
    return this.http.get<InternalOrder[]>(this.apiUrl);
  }

  create(payload: InternalOrderPayload): Observable<InternalOrder> {
    return this.http.post<InternalOrder>(this.apiUrl, payload);
  }

  updateStatus(orderId: number, status: InternalOrderStatus): Observable<InternalOrder> {
    return this.http.patch<InternalOrder>(`${this.apiUrl}/${orderId}/estado`, { estado: status });
  }

  generateTransfer(orderId: number): Observable<InventoryMovement[]> {
    return this.http.post<InventoryMovement[]>(`${this.apiUrl}/${orderId}/transferencia`, {});
  }
}
