import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { finalize, Observable, of, shareReplay, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Supplier {
  id: number;
  ruc: string;
  razonSocial: string;
  contacto: string;
  telefono: string;
  correo: string;
  direccion: string;
  productosSuministrados: string;
  historialEntregas: string;
  estado: number;
}

export interface SupplierPayload {
  ruc: string;
  razonSocial: string;
  contacto: string;
  telefono: string;
  correo: string;
  direccion: string;
  productosSuministrados: string;
  historialEntregas: string;
}

export interface SupplierDelivery {
  id: number;
  supplierId: number;
  supplierName: string;
  productId: number;
  productName: string;
  productSku: string;
  warehouseName: string;
  quantity: number;
  deliveredAt: string;
  notes: string;
}

export interface SupplierDeliveryPayload {
  proveedorId: number;
  productoId: number;
  almacenDestinoId: number;
  cantidad: number;
  observaciones: string;
}

export interface SunatRucResult {
  razonSocial: string;
  numeroDocumento: string;
  estado: string;
  condicion: string;
  direccion: string;
  distrito: string;
  provincia: string;
  departamento: string;
  agenteRetencion: boolean;
  buenContribuyente: boolean;
}

@Injectable({ providedIn: 'root' })
export class SuppliersService {
  private apiUrl = `${environment.apiUrl}/proveedores`;
  private suppliersCache: Supplier[] | null = null;
  private suppliersRequest$: Observable<Supplier[]> | null = null;

  constructor(private http: HttpClient) {}

  list(): Observable<Supplier[]> {
    if (this.suppliersCache) {
      return of(this.suppliersCache);
    }

    if (!this.suppliersRequest$) {
      this.suppliersRequest$ = this.http.get<Supplier[]>(this.apiUrl).pipe(
        tap((suppliers) => {
          this.suppliersCache = suppliers;
        }),
        finalize(() => {
          this.suppliersRequest$ = null;
        }),
        shareReplay(1)
      );
    }

    return this.suppliersRequest$;
  }

  create(payload: SupplierPayload): Observable<Supplier> {
    return this.http.post<Supplier>(this.apiUrl, payload).pipe(
      tap((supplier) => {
        this.suppliersCache = this.suppliersCache ? [supplier, ...this.suppliersCache] : [supplier];
      })
    );
  }

  update(supplierId: number, payload: SupplierPayload): Observable<Supplier> {
    return this.http.put<Supplier>(`${this.apiUrl}/${supplierId}`, payload).pipe(
      tap((updatedSupplier) => {
        this.suppliersCache = (this.suppliersCache ?? []).map((supplier) =>
          supplier.id === supplierId ? updatedSupplier : supplier
        );
      })
    );
  }

  toggleStatus(supplierId: number): Observable<Supplier> {
    return this.http.patch<Supplier>(`${this.apiUrl}/${supplierId}/estado`, {}).pipe(
      tap((updatedSupplier) => {
        this.suppliersCache = (this.suppliersCache ?? []).map((supplier) =>
          supplier.id === supplierId ? updatedSupplier : supplier
        );
      })
    );
  }

  delete(supplierId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${supplierId}`).pipe(
      tap(() => {
        this.suppliersCache = (this.suppliersCache ?? []).filter((supplier) => supplier.id !== supplierId);
      })
    );
  }

  listDeliveries(): Observable<SupplierDelivery[]> {
    return this.http.get<SupplierDelivery[]>(`${this.apiUrl}/entregas`);
  }

  registerDelivery(payload: SupplierDeliveryPayload): Observable<SupplierDelivery> {
    return this.http.post<SupplierDelivery>(`${this.apiUrl}/entregas`, payload);
  }

  lookupSunat(ruc: string): Observable<SunatRucResult> {
    return this.http.get<SunatRucResult>(`${this.apiUrl}/sunat/${ruc}`);
  }
}
