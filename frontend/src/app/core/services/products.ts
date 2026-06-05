import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Product {
  id: number;
  nombre: string;
  sku: string;
  precioCompra: number;
  precioVenta: number;
  stock: number;
  stockMinimo: number;
  categoriaId: number;
  categoriaNombre: string;
  almacenId: number;
  almacenNombre: string;
  imagen: string;
  estado: number;
}

export interface ProductPayload {
  nombre: string;
  sku: string;
  precioCompra: number;
  precioVenta: number;
  stock: number;
  stockMinimo: number;
  categoriaId: number;
  almacenId: number;
  imagen: string;
  estado: number;
}

@Injectable({ providedIn: 'root' })
export class ProductsService {
  private apiUrl = `${environment.apiUrl}/productos`;

  constructor(private http: HttpClient) {}

  list(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }

  create(payload: ProductPayload): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, payload);
  }

  update(id: number, payload: ProductPayload): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
