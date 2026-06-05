import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { InternalOrdersService } from '../../../core/services/internal-orders';
import { InventoryMovement, InventoryService } from '../../../core/services/inventory';
import { Product, ProductsService } from '../../../core/services/products';
import { Alerta, AlertasService } from '../../../core/services/alertas';
import { DeliveryRoute, RoutesService } from '../../../core/services/routes';
import { SalesService } from '../../../core/services/sales';
import { Supplier, SuppliersService } from '../../../core/services/suppliers';
import { Warehouse, WarehousesService } from '../../../core/services/warehouses';

interface DashboardCard {
  label: string;
  value: string;
  detail: string;
  tone: 'purple' | 'gold' | 'green' | 'red' | 'blue';
}

interface AlertItem {
  title: string;
  detail: string;
  tone: 'warning' | 'danger' | 'info' | 'success';
}

@Component({
  selector: 'app-dashboard-home',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard-home.html',
  styleUrl: './dashboard-home.css',
})
export class DashboardHome implements OnInit {
  cards: DashboardCard[] = [];
  alerts: AlertItem[] = [];
  latestMovements: InventoryMovement[] = [];
  topProducts: Product[] = [];
  routeHealth: DeliveryRoute[] = [];
  storeSummary: Array<{ name: string; city: string; stock: number; fill: number; type: string }> = [];
  categoryMix: Array<{ label: string; value: number; share: number }> = [];
  timelineBars: Array<{ day: string; inbound: number; outbound: number }> = [];
  suppliers: Supplier[] = [];
  centralWarehouse?: Warehouse;
  regionalCount = 0;
  storesCount = 0;
  routesInTransit = 0;
  totalRevenue = 0;

  constructor(
    private productsService: ProductsService,
    private warehousesService: WarehousesService,
    private inventoryService: InventoryService,
    private routesService: RoutesService,
    private salesService: SalesService,
    private suppliersService: SuppliersService,
    private internalOrdersService: InternalOrdersService,
    private alertasService: AlertasService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadDashboard();
  }

  private loadDashboard() {
    const today = new Date().toISOString().slice(0, 10);

    this.productsService.list().subscribe({
      next: (products) => {
        const lowStock = products.filter((item) => item.stock <= item.stockMinimo).length;
        const stockouts = products.filter((item) => item.stock === 0).length;
        const totalProducts = products.length;
        const totalInventoryValue = products.reduce((acc, item) => acc + item.precioCompra * item.stock, 0);

        this.topProducts = [...products].sort((a, b) => b.stock - a.stock).slice(0, 5);
        this.categoryMix = this.buildCategoryMix(products);

        this.upsertCard('Total productos', String(totalProducts), 'Catalogo logistico activo', 'purple');
        this.upsertCard('Stock critico', String(lowStock), 'Productos por debajo del minimo', 'red');
        this.upsertCard('Productos agotados', String(stockouts), 'Sin inventario disponible', 'gold');
        this.upsertCard(
          'Inventario valorizado',
          `S/ ${Math.round(totalInventoryValue).toLocaleString('es-PE')}`,
          'Costo estimado en red logistica',
          'green'
        );
        this.cdr.detectChanges();
      },
    });

    this.warehousesService.list().subscribe({
      next: (warehouses) => {
        this.centralWarehouse = warehouses.find((item) => item.tipo === 'CENTRAL');
        this.regionalCount = warehouses.filter((item) => item.tipo === 'REGIONAL').length;
        this.storesCount = warehouses.filter((item) => item.tipo === 'TIENDA').length;
        const capacityTotal = warehouses.reduce((acc, item) => acc + item.capacidad, 0);
        const occupiedTotal = warehouses.reduce((acc, item) => acc + item.ocupacion, 0);

        this.upsertCard('Tiendas conectadas', String(this.storesCount), 'Puntos finales de abastecimiento', 'blue');
        this.upsertCard('Centros regionales', String(this.regionalCount), 'Nodos intermedios habilitados', 'purple');
        this.upsertCard(
          'Ocupacion nacional',
          `${Math.round((occupiedTotal / Math.max(capacityTotal, 1)) * 100)}%`,
          'Uso consolidado de capacidades',
          'gold'
        );
        this.cdr.detectChanges();
      },
    });

    this.inventoryService.listWarehouseSummary().subscribe({
      next: (summary) => {
        this.storeSummary = summary
          .map((item) => ({
            name: item.warehouse.nombre,
            city: item.warehouse.ciudad,
            stock: item.stockTotal,
            fill: Math.min(Math.round((item.warehouse.ocupacion / Math.max(item.warehouse.capacidad, 1)) * 100), 100),
            type: item.warehouse.tipo,
          }))
          .sort((a, b) => b.stock - a.stock)
          .slice(0, 5);
        this.cdr.detectChanges();
      },
    });

    this.inventoryService.listMovements().subscribe({
      next: (movements) => {
        this.latestMovements = movements.slice(0, 6);
        this.timelineBars = this.buildTimeline(movements);
        if (!this.alerts.length) {
          this.alerts = this.buildAlerts(movements);
        }
        this.cdr.detectChanges();
      },
    });

    this.alertasService.list().subscribe({
      next: (alertas) => {
        this.alerts = alertas.slice(0, 5).map((alerta) => this.toDashboardAlert(alerta));
        this.cdr.detectChanges();
      },
    });

    this.routesService.list().subscribe({
      next: (routes) => {
        this.routeHealth = routes.slice(0, 4);
        this.routesInTransit = routes.filter((item) => item.estado === 'en ruta').length;
        this.upsertCard('Entregas en ruta', String(this.routesInTransit), 'Despachos actualmente movilizados', 'blue');
        this.cdr.detectChanges();
      },
    });

    this.suppliersService.list().subscribe({
      next: (suppliers) => {
        this.suppliers = suppliers;
        const activeSuppliers = suppliers.filter((item) => item.estado === 1).length;
        this.upsertCard('Proveedores activos', String(activeSuppliers), 'Abastecimiento del centro centralizado', 'green');
        this.cdr.detectChanges();
      },
    });

    this.internalOrdersService.list().subscribe({
      next: (orders) => {
        const pending = orders.filter((item) => item.status === 'Pendiente').length;
        const active = orders.filter((item) => ['Aprobado', 'Preparando', 'En ruta'].includes(item.status)).length;
        this.upsertCard('Pedidos pendientes', String(pending), 'Solicitudes listas para aprobacion', 'red');
        this.upsertCard('Transferencias activas', String(active), 'Pedidos aprobados o despachados', 'purple');
        this.cdr.detectChanges();
      },
    });

    this.salesService.list(today, today).subscribe({
      next: (sales) => {
        this.totalRevenue = sales.reduce((acc, item) => acc + item.ingresos, 0);
        this.upsertCard('Ingresos del dia', `S/ ${Math.round(this.totalRevenue)}`, 'Venta total de tiendas conectadas', 'gold');
        this.cdr.detectChanges();
      },
    });
  }

  private upsertCard(label: string, value: string, detail: string, tone: DashboardCard['tone']) {
    const current = this.cards.find((item) => item.label === label);
    if (current) {
      current.value = value;
      current.detail = detail;
      current.tone = tone;
      return;
    }

    this.cards = [...this.cards, { label, value, detail, tone }];
  }

  private buildAlerts(movements: InventoryMovement[]) {
    const transfers = movements.filter((item) => item.tipo === 'Transferencia').length;
    const incomes = movements.filter((item) => item.tipo === 'Ingreso').length;
    const outflows = movements.filter((item) => item.tipo !== 'Ingreso').length;

    return [
      {
        title: this.centralWarehouse?.nombre ?? 'Almacen Central Lima',
        detail: 'Punto unico de recepcion desde proveedores y despacho nacional.',
        tone: 'info' as const,
      },
      {
        title: `${transfers} transferencias monitoreadas`,
        detail: 'Movimientos entre central, regionales y tiendas con trazabilidad.',
        tone: 'warning' as const,
      },
      {
        title: `${incomes} ingresos y ${outflows} salidas`,
        detail: 'Balance reciente del circuito logistico y reposicion.',
        tone: 'success' as const,
      },
    ];
  }

  private toDashboardAlert(alerta: Alerta): AlertItem {
    return {
      title: alerta.titulo,
      detail: alerta.detalle,
      tone: alerta.severidad === 'CRITICA' ? 'danger' : alerta.severidad === 'ALTA' ? 'warning' : 'info',
    };
  }

  private buildCategoryMix(products: Product[]) {
    const totals = products.reduce((acc, item) => {
      const current = acc.get(item.categoriaNombre) ?? 0;
      acc.set(item.categoriaNombre, current + item.stock);
      return acc;
    }, new Map<string, number>());
    const grandTotal = Array.from(totals.values()).reduce((acc, item) => acc + item, 0);

    return Array.from(totals.entries())
      .map(([label, value]) => ({
        label,
        value,
        share: Math.round((value / Math.max(grandTotal, 1)) * 100),
      }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 5);
  }

  private buildTimeline(movements: InventoryMovement[]) {
    const labels = ['Lun', 'Mar', 'Mie', 'Jue', 'Vie', 'Sab'];
    return labels.map((day, index) => {
      const inbound = movements
        .filter((item) => item.tipo === 'Ingreso')
        .reduce((acc, item) => acc + item.cantidad, 0);
      const outbound = movements
        .filter((item) => item.tipo !== 'Ingreso')
        .reduce((acc, item) => acc + item.cantidad, 0);

      return {
        day,
        inbound: Math.max(18, Math.min(100, Math.round((inbound / Math.max(index + 2, 2)) % 100))),
        outbound: Math.max(16, Math.min(100, Math.round((outbound / Math.max(index + 3, 2)) % 100))),
      };
    });
  }
}
