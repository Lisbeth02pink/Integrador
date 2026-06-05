import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import * as L from 'leaflet';
import { InternalOrder, InternalOrdersService } from '../../../core/services/internal-orders';
import { InventoryTransfer } from '../../../core/services/inventory';
import {
  DeliveryRoute,
  DeliveryRoutePayload,
  DeliveryRouteStatus,
  RoutesService,
} from '../../../core/services/routes';

@Component({
  selector: 'app-routes-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './routes-page.html',
  styleUrl: './routes-page.css',
})
export class RoutesPage implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('routeMap') routeMapRef?: ElementRef<HTMLElement>;

  readonly statusOptions: DeliveryRouteStatus[] = [
    'pendiente',
    'preparado',
    'cargando',
    'en ruta',
    'detenido',
    'retrasado',
    'entregado',
    'cancelado',
  ];

  routes: DeliveryRoute[] = [];
  internalOrders: InternalOrder[] = [];
  transfers: InventoryTransfer[] = [];
  private map?: L.Map;
  private routeLayer?: L.LayerGroup;
  modalOpen = false;
  editingId: number | null = null;
  errorMessage = '';
  form = this.createEmptyForm();

  constructor(
    private routesService: RoutesService,
    private internalOrdersService: InternalOrdersService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadRoutes();
    this.loadInternalOrders();
    this.loadTransfers();
  }

  ngAfterViewInit() {
    this.initMap();
  }

  ngOnDestroy() {
    this.map?.remove();
  }

  get activeRoutes() {
    return this.routes.filter((item) => ['en ruta', 'detenido', 'retrasado', 'cargando'].includes(item.estado)).length;
  }

  get deliveredRoutes() {
    return this.routes.filter((item) => item.estado === 'entregado').length;
  }

  get averageProgress() {
    return this.routes.length
      ? Math.round(this.routes.reduce((acc, item) => acc + item.progreso, 0) / this.routes.length)
      : 0;
  }

  get delayedRoutes() {
    return this.routes.filter((item) => item.estado === 'retrasado').length;
  }

  get routableOrders() {
    return this.internalOrders.filter((order) => order.status !== 'Entregado');
  }

  get routableTransfers() {
    return this.transfers.filter((transfer) => transfer.estado !== 'CANCELADA');
  }

  openNewModal() {
    this.editingId = null;
    this.form = this.createEmptyForm();
    this.modalOpen = true;
  }

  editRoute(route: DeliveryRoute) {
    this.editingId = route.id;
    this.form = {
      ...this.createEmptyForm(),
      ...route,
      pedidoId: route.pedidoId ?? null,
      transferenciaId: route.transferenciaId ?? null,
      vehiculo: route.vehiculo ?? '',
      tipoVehiculo: route.tipoVehiculo ?? 'Camion ligero',
      placa: route.placa ?? '',
      capacidadVehiculo: route.capacidadVehiculo ?? 120,
      cantidadCarga: route.cantidadCarga ?? 30,
      origen: route.origen ?? '',
      destino: route.destino ?? '',
      fechaEntrega: route.fechaEntrega ?? new Date().toISOString().slice(0, 10),
      horaEstimadaLlegada: route.horaEstimadaLlegada ?? '13:00',
      horaEntregaReal: route.horaEntregaReal ?? '',
      ubicacionActual: route.ubicacionActual ?? '',
      observaciones: route.observaciones ?? '',
      incidencias: route.incidencias ?? '',
      estadoGps: route.estadoGps ?? 'online',
      evidenciaEntrega: route.evidenciaEntrega ?? '',
      firmaDigital: route.firmaDigital ?? '',
      fotoEntrega: route.fotoEntrega ?? '',
      vehiculoActivo: route.vehiculoActivo ?? true,
      conductorBloqueado: route.conductorBloqueado ?? false,
      confirmacionEntrega: route.confirmacionEntrega ?? false,
    };
    this.modalOpen = true;
  }

  closeModal() {
    this.modalOpen = false;
    this.editingId = null;
    this.errorMessage = '';
    this.form = this.createEmptyForm();
  }

  onOrderSelected(orderId: number | null) {
    const order = this.internalOrders.find((item) => item.id === Number(orderId));
    if (!order) return;

    this.form.transferenciaId = null;
    this.form.nombre = `Pedido interno #${order.id}`;
    this.form.zona = order.storeName;
    this.form.destino = order.storeName;
    this.form.pedidos = 1;
    this.form.cantidadCarga = Math.max(order.items.reduce((acc, item) => acc + item.quantity, 0), 1);
    this.form.observaciones = order.notes || `Pedido solicitado por ${order.requestedBy}`;
  }

  onTransferSelected(transferId: number | null) {
    const transfer = this.transfers.find((item) => item.id === Number(transferId));
    if (!transfer) return;

    this.form.pedidoId = null;
    this.form.nombre = `Transferencia #${transfer.id}`;
    this.form.zona = transfer.almacenDestinoNombre;
    this.form.origen = transfer.almacenOrigenNombre;
    this.form.destino = transfer.almacenDestinoNombre;
    this.form.pedidos = 1;
    this.form.cantidadCarga = Math.max(transfer.detalles.reduce((acc, item) => acc + item.cantidad, 0), 1);
    this.form.observaciones = transfer.referencia;
  }

  saveRoute() {
    this.errorMessage = '';

    if (!this.form.nombre.trim() || !this.form.repartidor.trim()) {
      this.errorMessage = 'La ruta debe tener nombre y conductor asignado.';
      return;
    }

    if (!this.form.vehiculo.trim() || !this.form.placa.trim()) {
      this.errorMessage = 'La ruta debe tener vehiculo y placa.';
      return;
    }

    if (!this.form.origen.trim() || !this.form.destino.trim() || this.form.origen.trim() === this.form.destino.trim()) {
      this.errorMessage = 'El origen y el destino deben estar definidos y ser diferentes.';
      return;
    }

    if (!this.form.capacidadVehiculo || !this.form.cantidadCarga || this.form.cantidadCarga > this.form.capacidadVehiculo) {
      this.errorMessage = 'La carga no puede exceder la capacidad del vehiculo.';
      return;
    }

    if (!this.form.pedidoId && !this.form.transferenciaId) {
      this.errorMessage = 'Asocia la ruta con un pedido interno o una transferencia.';
      return;
    }

    if (this.form.estado === 'entregado' && !this.form.confirmacionEntrega) {
      this.errorMessage = 'No se puede cerrar la ruta sin confirmacion de entrega.';
      return;
    }

    const payload: DeliveryRoutePayload = {
      nombre: this.form.nombre.trim(),
      zona: this.form.zona.trim(),
      repartidor: this.form.repartidor.trim(),
      pedidoId: this.form.pedidoId || null,
      transferenciaId: this.form.transferenciaId || null,
      vehiculo: this.form.vehiculo.trim(),
      tipoVehiculo: this.form.tipoVehiculo.trim(),
      placa: this.form.placa.trim().toUpperCase(),
      capacidadVehiculo: Number(this.form.capacidadVehiculo),
      cantidadCarga: Number(this.form.cantidadCarga),
      origen: this.form.origen.trim(),
      destino: this.form.destino.trim(),
      fechaEntrega: this.form.fechaEntrega,
      pedidos: Number(this.form.pedidos),
      estado: this.form.estado,
      horaSalida: this.form.horaSalida,
      horaEstimadaLlegada: this.form.horaEstimadaLlegada,
      horaEntregaReal: this.form.horaEntregaReal || null,
      ubicacionActual: this.form.ubicacionActual.trim() || null,
      observaciones: this.form.observaciones.trim() || null,
      incidencias: this.form.incidencias.trim() || null,
      estadoGps: this.form.estadoGps.trim() || null,
      evidenciaEntrega: this.form.evidenciaEntrega.trim() || null,
      firmaDigital: this.form.firmaDigital.trim() || null,
      fotoEntrega: this.form.fotoEntrega.trim() || null,
      vehiculoActivo: this.form.vehiculoActivo,
      conductorBloqueado: this.form.conductorBloqueado,
      confirmacionEntrega: this.form.confirmacionEntrega,
      progreso: Number(this.form.progreso),
    };

    const request$ = this.editingId
      ? this.routesService.update(this.editingId, payload)
      : this.routesService.create(payload);

    request$.subscribe({
      next: () => {
        this.loadRoutes();
        this.closeModal();
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo guardar la ruta.';
        this.cdr.detectChanges();
      },
    });
  }

  deleteRoute(route: DeliveryRoute) {
    this.routesService.delete(route.id).subscribe({
      next: () => this.loadRoutes(),
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo eliminar la ruta.';
        this.cdr.detectChanges();
      },
    });
  }

  statusTone(route: DeliveryRoute) {
    if (route.estado === 'entregado') {
      return 'is-success';
    }
    if (route.estado === 'retrasado' || route.estado === 'detenido') {
      return 'is-warning';
    }
    if (route.estado === 'en ruta' || route.estado === 'cargando') {
      return 'is-info';
    }
    if (route.estado === 'cancelado') {
      return 'is-danger';
    }
    return '';
  }

  prettyStatus(status: DeliveryRouteStatus) {
    return status.charAt(0).toUpperCase() + status.slice(1);
  }

  private loadRoutes() {
    this.routesService.list().subscribe({
      next: (routes) => {
        this.routes = routes.map((route, index) => ({
          ...route,
          vehiculo: route.vehiculo || `Camion ${index + 1}`,
          tipoVehiculo: route.tipoVehiculo || 'Camion ligero',
          placa: route.placa || `TMB-${120 + index}`,
          origen: route.origen || 'Almacen Central Lima',
          destino: route.destino || route.zona,
          horaEstimadaLlegada: route.horaEstimadaLlegada || '13:00',
          fechaEntrega: route.fechaEntrega || new Date().toISOString().slice(0, 10),
          capacidadVehiculo: route.capacidadVehiculo ?? 120,
          cantidadCarga: route.cantidadCarga ?? Math.max(route.pedidos * 4, 1),
          ubicacionActual: route.ubicacionActual || 'Lima',
          estadoGps: route.estadoGps || 'online',
          vehiculoActivo: route.vehiculoActivo ?? true,
          conductorBloqueado: route.conductorBloqueado ?? false,
          confirmacionEntrega: route.confirmacionEntrega ?? route.estado === 'entregado',
          observaciones: route.observaciones || '',
          incidencias: route.incidencias || '',
          evidenciaEntrega: route.evidenciaEntrega || '',
          firmaDigital: route.firmaDigital || '',
          fotoEntrega: route.fotoEntrega || '',
        }));
        this.renderMapRoutes();
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar las rutas.';
        this.cdr.detectChanges();
      },
    });
  }

  private loadInternalOrders() {
    this.internalOrdersService.list().subscribe({
      next: (orders) => {
        this.internalOrders = orders;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los pedidos internos para rutas.';
        this.cdr.detectChanges();
      },
    });
  }

  private loadTransfers() {
    this.routesService.listAvailableTransfers().subscribe({
      next: (transfers) => {
        this.transfers = transfers;
        this.cdr.detectChanges();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = `No se pudieron cargar las transferencias para rutas. Estado: ${error.status || 'sin respuesta'}.`;
        this.cdr.detectChanges();
      },
    });
  }

  private initMap() {
    if (!this.routeMapRef || this.map) return;

    this.map = L.map(this.routeMapRef.nativeElement, {
      center: [-9.19, -75.0152],
      zoom: 5,
      scrollWheelZoom: false,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    this.routeLayer = L.layerGroup().addTo(this.map);
    setTimeout(() => this.map?.invalidateSize(), 0);
    this.renderMapRoutes();
  }

  private renderMapRoutes() {
    if (!this.map || !this.routeLayer) return;

    this.routeLayer.clearLayers();
    const central = this.locationFor('Almacen Central Lima');

    L.circleMarker(central, {
      radius: 9,
      color: '#5f2a7a',
      fillColor: '#5f2a7a',
      fillOpacity: 0.9,
    }).bindPopup('Almacen Central Lima').addTo(this.routeLayer);

    const bounds: L.LatLngExpression[] = [central];
    this.routes.forEach((route) => {
      const destination = this.locationFor(route.destino || route.zona);
      bounds.push(destination);

      L.polyline([central, destination], {
        color: this.routeColor(route.estado),
        weight: 4,
        opacity: 0.75,
      }).addTo(this.routeLayer!);

      L.circleMarker(destination, {
        radius: 7,
        color: this.routeColor(route.estado),
        fillColor: this.routeColor(route.estado),
        fillOpacity: 0.85,
      })
        .bindPopup(`${route.destino || route.zona}<br>${route.vehiculo || 'Vehiculo'} - ETA ${route.horaEstimadaLlegada || 'sin dato'}`)
        .addTo(this.routeLayer!);
    });

    if (bounds.length > 1) {
      this.map.fitBounds(L.latLngBounds(bounds), { padding: [24, 24] });
    }
  }

  private locationFor(value: string): L.LatLngExpression {
    const normalized = value.toLowerCase();
    if (normalized.includes('piura')) return [-5.1945, -80.6328];
    if (normalized.includes('chiclayo')) return [-6.7714, -79.8409];
    if (normalized.includes('trujillo')) return [-8.1116, -79.0287];
    if (normalized.includes('arequipa')) return [-16.409, -71.5375];
    if (normalized.includes('cusco')) return [-13.5319, -71.9675];
    return [-12.0464, -77.0428];
  }

  private routeColor(status: DeliveryRouteStatus) {
    if (status === 'entregado') return '#16834a';
    if (status === 'retrasado' || status === 'detenido') return '#d97706';
    if (status === 'cancelado') return '#b42318';
    return '#5f2a7a';
  }

  private createEmptyForm() {
    return {
      nombre: '',
      zona: '',
      repartidor: '',
      pedidoId: null as number | null,
      transferenciaId: null as number | null,
      vehiculo: '',
      tipoVehiculo: 'Camion ligero',
      placa: '',
      capacidadVehiculo: 120,
      cantidadCarga: 30,
      origen: 'Almacen Central Lima',
      destino: '',
      fechaEntrega: new Date().toISOString().slice(0, 10),
      pedidos: 1,
      estado: 'pendiente' as DeliveryRouteStatus,
      horaSalida: '09:00',
      horaEstimadaLlegada: '13:00',
      horaEntregaReal: '',
      ubicacionActual: '',
      observaciones: '',
      incidencias: '',
      estadoGps: 'online',
      evidenciaEntrega: '',
      firmaDigital: '',
      fotoEntrega: '',
      vehiculoActivo: true,
      conductorBloqueado: false,
      confirmacionEntrega: false,
      progreso: 0,
    };
  }
}
