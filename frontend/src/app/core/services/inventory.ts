import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Product } from './products';
import { Warehouse } from './warehouses';

export interface InventoryMovement {
  id: number;
  fecha: string;
  productoId: number;
  productoSku: string;
  productoNombre: string;
  tipo: string;
  cantidad: number;
  almacenOrigen: string | null;
  almacenDestino: string | null;
  referencia: string;
  usuario?: string;
}

export interface InventoryTransferDetail {
  productoId: number;
  productoNombre: string;
  productoSku: string;
  cantidad: number;
}

export interface InventoryTransfer {
  id: number;
  almacenOrigenId: number;
  almacenOrigenNombre: string;
  almacenDestinoId: number;
  almacenDestinoNombre: string;
  fecha: string;
  estado: string;
  responsable: string;
  referencia: string;
  detalles: InventoryTransferDetail[];
}

export interface ReceptionPayload {
  transferenciaId: number;
  responsable: string;
  observaciones: string;
}

export interface InventoryWarehouseSummary {
  warehouse: Warehouse;
  totalProductos: number;
  stockTotal: number;
}

export interface TransferPayload {
  productoId: number;
  almacenOrigenId: number;
  almacenDestinoId: number;
  cantidad: number;
}

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private apiUrl = `${environment.apiUrl}/inventario`;

  constructor(private http: HttpClient) {}

  listMovements(): Observable<InventoryMovement[]> {
    return this.http.get<InventoryMovement[]>(`${this.apiUrl}/movimientos`);
  }

  listTransfers(): Observable<InventoryTransfer[]> {
    return this.http.get<InventoryTransfer[]>(`${this.apiUrl}/transferencias`);
  }

  listLowStock(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/stock-bajo`);
  }

  listWarehouseSummary(): Observable<InventoryWarehouseSummary[]> {
    return this.http.get<InventoryWarehouseSummary[]>(`${this.apiUrl}/resumen-almacenes`);
  }

  transfer(payload: TransferPayload): Observable<InventoryMovement> {
    return this.http.post<InventoryMovement>(`${this.apiUrl}/transferencias`, payload);
  }

  confirmReception(payload: ReceptionPayload): Observable<InventoryTransfer> {
    return this.http.post<InventoryTransfer>(`${environment.apiUrl}/recepcion-tienda`, payload);
  }
}
