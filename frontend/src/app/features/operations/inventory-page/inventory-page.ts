import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Product, ProductsService } from '../../../core/services/products';
import { Warehouse, WarehousesService } from '../../../core/services/warehouses';
import {
  InventoryMovement,
  InventoryService,
  InventoryWarehouseSummary,
} from '../../../core/services/inventory';

@Component({
  selector: 'app-inventory-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './inventory-page.html',
  styleUrl: './inventory-page.css',
})
export class InventoryPage implements OnInit {
  warehouses: Warehouse[] = [];
  products: Product[] = [];
  movements: InventoryMovement[] = [];
  warehouseSummary: InventoryWarehouseSummary[] = [];
  stockAlerts: Product[] = [];

  loading = false;
  warehouseFilter = 'todos';

  constructor(
    private inventoryService: InventoryService,
    private warehousesService: WarehousesService,
    private productsService: ProductsService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  get totalStock(): number {
    return this.warehouseSummary.reduce((acc, item) => acc + item.stockTotal, 0);
  }

  get totalProducts(): number {
    return this.products.filter((p) => p.estado === 1).length;
  }

  get totalInventoryValue(): number {
    return this.products.reduce(
      (acc, p) => acc + (p.precioVenta ?? 0) * (p.stock ?? 0),
      0,
    );
  }

  get activeAlerts(): number {
    return this.stockAlerts.length;
  }

  get centralWarehouse(): Warehouse | undefined {
    return this.warehouses.find((w) => w.tipo === 'CENTRAL');
  }

  get regionalWarehouses(): Warehouse[] {
    return this.warehouses.filter((w) => w.tipo === 'REGIONAL');
  }

  get storeWarehouses(): Warehouse[] {
    return this.warehouses.filter((w) => w.tipo === 'TIENDA');
  }

  get transitToRegional(): number {
    return this.movements
      .filter(
        (m) =>
          m.tipo === 'Transferencia' &&
          this.regionalWarehouses.some((w) => w.nombre === m.almacenDestino),
      )
      .reduce((acc, m) => acc + m.cantidad, 0);
  }

  get transitToStores(): number {
    return this.movements
      .filter(
        (m) =>
          m.tipo === 'Transferencia' &&
          this.storeWarehouses.some((w) => w.nombre === m.almacenDestino),
      )
      .reduce((acc, m) => acc + m.cantidad, 0);
  }

  get filteredMovements(): InventoryMovement[] {
    if (this.warehouseFilter === 'todos') return this.movements;
    return this.movements.filter(
      (m) =>
        m.almacenOrigen === this.warehouseFilter ||
        m.almacenDestino === this.warehouseFilter,
    );
  }

  getAlertLevel(product: Product): 'CRITICO' | 'BAJO' | 'NORMAL' {
    const stock = product.stock ?? 0;
    const min = product.stockMinimo ?? 0;
    if (stock <= min * 0.5) return 'CRITICO';
    if (stock <= min) return 'BAJO';
    return 'NORMAL';
  }

  warehouseTypeLabel(type: Warehouse['tipo']): string {
    if (type === 'CENTRAL') return 'Almacen central';
    if (type === 'REGIONAL') return 'Hub regional';
    return 'Tienda';
  }

  warehouseOccupancy(summary: InventoryWarehouseSummary): number {
    const capacity = summary.warehouse.capacidad || 0;
    if (!capacity) return 0;
    return Math.min(100, Math.round((summary.stockTotal / capacity) * 100));
  }

  movementClass(type: string): string {
    if (type === 'Ingreso') return 'entrada';
    if (type === 'Egreso') return 'salida';
    return 'transferencia';
  }

  movementSign(movement: InventoryMovement): string {
    if (movement.tipo === 'Ingreso') return `+${movement.cantidad}`;
    if (movement.tipo === 'Egreso') return `-${movement.cantidad}`;
    return `${movement.cantidad}`;
  }

  formatValue(value: number): string {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: 'PEN',
      minimumFractionDigits: 0,
    }).format(value);
  }

  formatNumber(value: number): string {
    return new Intl.NumberFormat('es-PE').format(value);
  }

  exportKardex(): void {
    const headers = ['Fecha', 'Producto', 'Tipo', 'Cantidad', 'Origen', 'Destino', 'Referencia', 'Usuario'];
    const rows = this.filteredMovements.map((m) => [
      m.fecha,
      m.productoNombre,
      m.tipo,
      m.cantidad,
      m.almacenOrigen || '-',
      m.almacenDestino || '-',
      m.referencia || '-',
      m.usuario || '-',
    ]);

    const csv = [headers, ...rows].map((r) => r.join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `kardex-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  private loadAll(): void {
    this.loading = true;

    this.warehousesService.list().subscribe({
      next: (warehouses) => {
        this.warehouses = warehouses;
        this.cdr.detectChanges();
      },
    });

    this.productsService.list().subscribe({
      next: (products) => {
        this.products = products;
        this.cdr.detectChanges();
      },
    });

    this.inventoryService.listMovements().subscribe({
      next: (movements) => {
        this.movements = movements;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
    });

    this.inventoryService.listLowStock().subscribe({
      next: (alerts) => {
        this.stockAlerts = alerts;
        this.cdr.detectChanges();
      },
    });

    this.inventoryService.listWarehouseSummary().subscribe({
      next: (summary) => {
        this.warehouseSummary = summary;
        this.cdr.detectChanges();
      },
    });
  }
}
