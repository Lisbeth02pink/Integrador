import { CommonModule, CurrencyPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { InternalOrder, InternalOrdersService } from '../../../core/services/internal-orders';
import { InventoryMovement, InventoryService, InventoryWarehouseSummary } from '../../../core/services/inventory';
import { Product } from '../../../core/services/products';
import { SaleSummary, SalesService } from '../../../core/services/sales';
import { Supplier, SupplierDelivery, SuppliersService } from '../../../core/services/suppliers';

@Component({
  selector: 'app-reports-page',
  imports: [CommonModule, FormsModule, CurrencyPipe],
  templateUrl: './reports-page.html',
  styleUrl: './reports-page.css',
})
export class ReportsPage implements OnInit {
  fromDate = '2026-04-01';
  toDate = '2026-05-06';

  movements: InventoryMovement[] = [];
  lowStock: Product[] = [];
  warehouseSummary: InventoryWarehouseSummary[] = [];
  orders: InternalOrder[] = [];
  suppliers: Supplier[] = [];
  deliveries: SupplierDelivery[] = [];
  sales: SaleSummary[] = [];
  errorMessage = '';
  loading = false;

  constructor(
    private inventoryService: InventoryService,
    private internalOrdersService: InternalOrdersService,
    private suppliersService: SuppliersService,
    private salesService: SalesService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadReports();
  }

  get ingresosPeriodo() {
    return this.sales.reduce((acc, item) => acc + item.ingresos, 0);
  }

  get egresosPeriodo() {
    return this.sales.reduce((acc, item) => acc + item.egresos, 0);
  }

  get utilidadPeriodo() {
    return this.ingresosPeriodo - this.egresosPeriodo;
  }

  get transferencias() {
    return this.movements.filter((item) => item.tipo === 'Transferencia');
  }

  get ingresosProveedor() {
    return this.movements.filter((item) => item.tipo === 'Ingreso');
  }

  get pedidosPendientes() {
    return this.orders.filter((item) => item.status === 'Pendiente').length;
  }

  get pedidosEnProceso() {
    return this.orders.filter((item) => ['Aprobado', 'Preparando', 'En ruta'].includes(item.status)).length;
  }

  get pedidosEntregados() {
    return this.orders.filter((item) => item.status === 'Entregado').length;
  }

  get proveedoresActivos() {
    return this.suppliers.filter((item) => item.estado === 1).length;
  }

  get totalStockTiendas() {
    const centralId = this.centralWarehouseId;
    return this.warehouseSummary
      .filter((item) => item.warehouse.id !== centralId)
      .reduce((acc, item) => acc + item.stockTotal, 0);
  }

  get stockCentral() {
    return this.warehouseSummary.find((item) => item.warehouse.id === this.centralWarehouseId)?.stockTotal ?? 0;
  }

  get topSuppliers() {
    return [...this.deliveries]
      .reduce((acc, item) => {
        const current = acc.get(item.supplierName) ?? 0;
        acc.set(item.supplierName, current + item.quantity);
        return acc;
      }, new Map<string, number>())
      .entries();
  }

  get topSuppliersList() {
    return Array.from(this.topSuppliers)
      .map(([name, quantity]) => ({ name, quantity }))
      .sort((a, b) => b.quantity - a.quantity)
      .slice(0, 5);
  }

  get topProductsMoved() {
    return Array.from(
      this.movements.reduce((acc, item) => {
        const current = acc.get(item.productoNombre) ?? 0;
        acc.set(item.productoNombre, current + item.cantidad);
        return acc;
      }, new Map<string, number>())
    )
      .map(([name, quantity]) => ({ name, quantity }))
      .sort((a, b) => b.quantity - a.quantity)
      .slice(0, 5);
  }

  private get centralWarehouseId() {
    return [...this.warehouseSummary]
      .sort((a, b) => {
        const aCentral = a.warehouse.nombre.toLowerCase().includes('central') ? 1 : 0;
        const bCentral = b.warehouse.nombre.toLowerCase().includes('central') ? 1 : 0;
        if (aCentral !== bCentral) {
          return bCentral - aCentral;
        }
        return b.stockTotal - a.stockTotal;
      })[0]?.warehouse.id;
  }

  loadReports() {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      movements: this.inventoryService.listMovements(),
      lowStock: this.inventoryService.listLowStock(),
      warehouseSummary: this.inventoryService.listWarehouseSummary(),
      orders: this.internalOrdersService.list(),
      suppliers: this.suppliersService.list(),
      deliveries: this.suppliersService.listDeliveries(),
      sales: this.salesService.list(this.fromDate, this.toDate),
    }).subscribe({
      next: ({ movements, lowStock, warehouseSummary, orders, suppliers, deliveries, sales }) => {
        this.movements = movements;
        this.lowStock = lowStock;
        this.warehouseSummary = warehouseSummary;
        this.orders = orders;
        this.suppliers = suppliers;
        this.deliveries = deliveries;
        this.sales = sales;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'No se pudieron cargar los reportes consolidados.';
        this.cdr.detectChanges();
      },
    });
  }
}
