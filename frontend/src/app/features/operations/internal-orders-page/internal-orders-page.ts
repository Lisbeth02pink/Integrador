import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import Swal from 'sweetalert2';
import {
  InternalOrder,
  InternalOrderItem,
  InternalOrderPriority,
  InternalOrderStatus,
  InternalOrdersService,
} from '../../../core/services/internal-orders';
import { Product, ProductsService } from '../../../core/services/products';
import { Warehouse, WarehousesService } from '../../../core/services/warehouses';

@Component({
  selector: 'app-internal-orders-page',
  imports: [CommonModule, FormsModule, DatePipe, RouterLink],
  templateUrl: './internal-orders-page.html',
  styleUrl: './internal-orders-page.css',
})
export class InternalOrdersPage implements OnInit {
  orders: InternalOrder[] = [];
  stores: Warehouse[] = [];
  products: Product[] = [];
  errorMessage = '';

  form = this.createEmptyForm();
  draftItems: InternalOrderItem[] = [];

  constructor(
    private ordersService: InternalOrdersService,
    private warehousesService: WarehousesService,
    private productsService: ProductsService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadData();
  }

  get productsFromCentral() {
    const centralName = this.getCentralWarehouseName();
    return this.products.filter((item) => item.almacenNombre === centralName);
  }

  get pendingCount() {
    return this.orders.filter((item) => item.status === 'Pendiente').length;
  }

  get activeCount() {
    return this.orders.filter((item) => ['Aprobado', 'Preparando', 'En ruta'].includes(item.status)).length;
  }

  get deliveredCount() {
    return this.orders.filter((item) => item.status === 'Entregado').length;
  }

  addDraftItem() {
    const product = this.productsFromCentral.find((item) => item.id === Number(this.form.productId));
    const quantity = Number(this.form.quantity);

    if (!product || quantity <= 0) {
      this.errorMessage = 'Selecciona un producto valido y una cantidad mayor a cero.';
      return;
    }

    const existing = this.draftItems.find((item) => item.productId === product.id);
    if (existing) {
      existing.quantity += quantity;
    } else {
      this.draftItems = [
        ...this.draftItems,
        {
          productId: product.id,
          productName: product.nombre,
          sku: product.sku,
          quantity,
        },
      ];
    }

    this.form.productId = 0;
    this.form.quantity = 0;
    this.errorMessage = '';
  }

  removeDraftItem(productId: number) {
    this.draftItems = this.draftItems.filter((item) => item.productId !== productId);
  }

  saveOrder() {
    const store = this.stores.find((item) => item.id === Number(this.form.storeId));
    if (!store || !this.form.requestedBy.trim() || !this.draftItems.length) {
      this.errorMessage = 'Completa la tienda, el solicitante y agrega al menos un producto.';
      return;
    }

    this.ordersService.create({
      tiendaId: store.id,
      solicitadoPor: this.form.requestedBy.trim(),
      prioridad: this.form.priority,
      observaciones: this.form.notes.trim(),
      items: this.draftItems.map((item) => ({
        productoId: item.productId,
        cantidad: item.quantity,
      })),
    }).subscribe({
      next: async () => {
        this.resetForm();
        this.loadOrders();
        this.cdr.detectChanges();
        await Swal.fire({
          icon: 'success',
          title: 'Pedido registrado',
          text: 'La solicitud de la tienda ya entro al flujo del almacen central.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo registrar el pedido interno.';
        this.cdr.detectChanges();
      },
    });
  }

  updateStatus(order: InternalOrder, status: InternalOrderStatus) {
    this.ordersService.updateStatus(order.id, status).subscribe({
      next: () => {
        this.loadOrders();
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo actualizar el estado del pedido.';
        this.cdr.detectChanges();
      },
    });
  }

  generateTransfer(order: InternalOrder) {
    this.ordersService.generateTransfer(order.id).subscribe({
      next: async () => {
        this.loadOrders();
        await Swal.fire({
          icon: 'success',
          title: 'Transferencia generada',
          text: 'El stock salio del almacen central y el Kardex fue actualizado.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo generar la transferencia del pedido.';
        this.cdr.detectChanges();
      },
    });
  }

  private loadData() {
    this.loadOrders();

    this.warehousesService.list().subscribe({
      next: (warehouses) => {
        this.stores = warehouses.filter((item) => item.tipo === 'TIENDA');
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar las tiendas para pedidos internos.';
        this.cdr.detectChanges();
      },
    });

    this.productsService.list().subscribe({
      next: (products) => {
        this.products = products;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los productos del almacen central.';
        this.cdr.detectChanges();
      },
    });
  }

  private loadOrders() {
    this.ordersService.list().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los pedidos internos.';
        this.cdr.detectChanges();
      },
    });
  }

  private getCentralWarehouseName() {
    const centralWarehouse = this.products.find((item) => item.almacenNombre.toLowerCase().includes('central'));
    return centralWarehouse?.almacenNombre ?? '';
  }

  private createEmptyForm() {
    return {
      storeId: 0,
      requestedBy: '',
      priority: 'Media' as InternalOrderPriority,
      productId: 0,
      quantity: 0,
      notes: '',
    };
  }

  private resetForm() {
    this.form = this.createEmptyForm();
    this.draftItems = [];
    this.errorMessage = '';
  }
}
