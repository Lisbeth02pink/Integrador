import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { finalize, Observable, of, shareReplay, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Cliente {
  id: number;
  tipoDocumento: 'DNI' | 'RUC';
  documento: string;
  nombre: string;
  telefono: string;
  correo: string;
  direccion: string;
  estado: number;
}

export interface ClientePayload {
  tipoDocumento: 'DNI' | 'RUC';
  documento: string;
  nombre: string;
  telefono: string;
  correo: string;
  direccion: string;
}

@Injectable({
  providedIn: 'root',
})
export class ClientesService {
  private apiUrl = `${environment.apiUrl}/clientes`;
  private clientesCache: Cliente[] | null = null;
  private clientesRequest$: Observable<Cliente[]> | null = null;

  constructor(private http: HttpClient) {}

  list(): Observable<Cliente[]> {
    if (this.clientesCache) {
      return of(this.clientesCache);
    }

    if (!this.clientesRequest$) {
      this.clientesRequest$ = this.http.get<Cliente[]>(this.apiUrl).pipe(
        tap((clientes) => {
          this.clientesCache = clientes;
        }),
        finalize(() => {
          this.clientesRequest$ = null;
        }),
        shareReplay(1)
      );
    }

    return this.clientesRequest$;
  }

  create(payload: ClientePayload): Observable<Cliente> {
    return this.http.post<Cliente>(this.apiUrl, payload).pipe(
      tap((cliente) => {
        this.clientesCache = this.clientesCache ? [cliente, ...this.clientesCache] : [cliente];
      })
    );
  }

  update(clienteId: number, payload: ClientePayload): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.apiUrl}/${clienteId}`, payload).pipe(
      tap((updatedCliente) => {
        this.clientesCache = (this.clientesCache ?? []).map((cliente) =>
          cliente.id === clienteId ? updatedCliente : cliente
        );
      })
    );
  }

  toggleStatus(clienteId: number): Observable<Cliente> {
    return this.http.patch<Cliente>(`${this.apiUrl}/${clienteId}/estado`, {}).pipe(
      tap((updatedCliente) => {
        this.clientesCache = (this.clientesCache ?? []).map((cliente) =>
          cliente.id === clienteId ? updatedCliente : cliente
        );
      })
    );
  }

  delete(clienteId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${clienteId}`).pipe(
      tap(() => {
        this.clientesCache = (this.clientesCache ?? []).filter((cliente) => cliente.id !== clienteId);
      })
    );
  }
}
