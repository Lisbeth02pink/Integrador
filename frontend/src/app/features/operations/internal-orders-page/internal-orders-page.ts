import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
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
  imports: [CommonModule, ReactiveFormsModule, DatePipe, RouterLink],
  templateUrl: './internal-orders-page.html',
  styleUrl: './internal-orders-page.css',
})
export class InternalOrdersPage implements OnInit {
  orders: InternalOrder[] = [];
  stores: Warehouse[] = [];
  products: Product[] = [];
  errorMessage = '';

  orderForm: FormGroup;
  itemForm: FormGroup;
  draftItems: InternalOrderItem[] = [];

  constructor(
    private ordersService: InternalOrdersService,
    private warehousesService: WarehousesService,
    private productsService: ProductsService,
    private cdr: ChangeDetectorRef,
    private fb: FormBuilder
  ) {
    this.orderForm = this.fb.group({
      storeId: ['', [Validators.required]],
      requestedBy: ['', [Validators.required, Validators.minLength(3)]],
      priority: ['Media', [Validators.required]],
      notes: ['']
    });

    this.itemForm = this.fb.group({
      productId: ['', [Validators.required]],
      quantity: [1, [Validators.required, Validators.min(1)]]
    });
  }

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

  isFieldInvalid(form: FormGroup, field: string): boolean {
    const control = form.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  addDraftItem() {
    this.itemForm.markAllAsTouched();
    if (this.itemForm.invalid) {
      this.errorMessage = 'Selecciona un producto válido y una cantidad mayor a cero.';
      return;
    }

    const productId = Number(this.itemForm.get('productId')?.value);
    const quantity = Number(this.itemForm.get('quantity')?.value);
    const product = this.productsFromCentral.find((item) => item.id === productId);

    if (!product) return;

    // Calcular cuánto se ha pedido ya de este producto en el borrador
    const existing = this.draftItems.find((item) => item.productId === product.id);
    const pendingQuantity = existing ? existing.quantity + quantity : quantity;

    if (pendingQuantity > product.stock) {
       this.errorMessage = `¡Stock insuficiente! El Almacén Central solo tiene ${product.stock} unidades de ${product.nombre}.`;
       return;
    }

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

    this.itemForm.reset({ productId: '', quantity: 1 });
    this.errorMessage = '';
  }

  removeDraftItem(productId: number) {
    this.draftItems = this.draftItems.filter((item) => item.productId !== productId);
  }

  saveOrder() {
    this.orderForm.markAllAsTouched();
    this.errorMessage = '';

    if (this.orderForm.invalid) {
      this.errorMessage = 'Completa los campos obligatorios del pedido.';
      return;
    }

    if (!this.draftItems.length) {
      this.errorMessage = 'Agrega al menos un producto al pedido.';
      return;
    }

    const formValues = this.orderForm.value;

    this.ordersService.create({
      tiendaId: Number(formValues.storeId),
      solicitadoPor: formValues.requestedBy.trim(),
      prioridad: formValues.priority,
      observaciones: formValues.notes ? formValues.notes.trim() : '',
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
          text: 'La solicitud de la tienda ya entró al flujo del almacén central.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        let msg = 'No se pudo registrar el pedido interno.';
        if (error.error && typeof error.error === 'object') {
           msg = error.error.message || JSON.stringify(error.error);
        } else if (error.message) {
           msg = error.message;
        }
        this.errorMessage = `Error ${error.status || 'Desconocido'}: ${msg}`;
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
          text: 'El stock salió del almacén central y el Kardex fue actualizado.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        let msg = 'No se pudo generar la transferencia.';
        if (error.error && typeof error.error === 'object') {
           msg = error.error.message || JSON.stringify(error.error);
        } else if (error.message) {
           msg = error.message;
        }
        this.errorMessage = `Error ${error.status || 'Desconocido'}: ${msg}`;
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
        this.errorMessage = 'No se pudieron cargar los productos del almacén central.';
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

  private resetForm() {
    this.orderForm.reset({
      storeId: '',
      requestedBy: '',
      priority: 'Media',
      notes: ''
    });
    this.itemForm.reset({ productId: '', quantity: 1 });
    this.draftItems = [];
    this.errorMessage = '';
  }
}
