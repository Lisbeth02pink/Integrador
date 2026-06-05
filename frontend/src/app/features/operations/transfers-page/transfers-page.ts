import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { InventoryMovement, InventoryService } from '../../../core/services/inventory';
import { Product, ProductsService } from '../../../core/services/products';
import { Warehouse, WarehousesService } from '../../../core/services/warehouses';

@Component({
  selector: 'app-transfers-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './transfers-page.html',
  styleUrl: './transfers-page.css',
})
export class TransfersPage implements OnInit {
  movements: InventoryMovement[] = [];
  products: Product[] = [];
  warehouses: Warehouse[] = [];
  errorMessage = '';

  form = {
    productId: 0,
    destinationWarehouseId: 0,
    quantity: 0,
    scheduledDate: '',
    observation: '',
  };

  constructor(
    private inventoryService: InventoryService,
    private productsService: ProductsService,
    private warehousesService: WarehousesService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadData();
  }

  get centralWarehouse() {
    return this.getCentralWarehouse(this.warehouses);
  }

  get destinationStores() {
    const centralId = this.centralWarehouse?.id;
    return this.warehouses.filter((item) => item.id !== centralId && item.tipo === 'TIENDA');
  }

  get centralProducts() {
    const centralName = this.centralWarehouse?.nombre;
    return this.products.filter((item) => item.almacenNombre === centralName);
  }

  get transferMovements() {
    return this.movements.filter((item) => item.tipo === 'Transferencia');
  }

  get pendingLikeTransfers() {
    return this.transferMovements.filter((item) => item.referencia?.toLowerCase().includes('pedido interno')).length;
  }

  registerTransfer() {
    this.errorMessage = '';

    if (!this.centralWarehouse) {
      this.errorMessage = 'No se encontro un almacen central configurado.';
      return;
    }
    if (!this.form.productId || !this.form.destinationWarehouseId || Number(this.form.quantity) <= 0) {
      this.errorMessage = 'Selecciona producto, tienda destino y una cantidad valida.';
      return;
    }

    this.inventoryService.transfer({
      productoId: Number(this.form.productId),
      almacenOrigenId: this.centralWarehouse.id,
      almacenDestinoId: Number(this.form.destinationWarehouseId),
      cantidad: Number(this.form.quantity),
    }).subscribe({
      next: async () => {
        this.form = {
          productId: 0,
          destinationWarehouseId: 0,
          quantity: 0,
          scheduledDate: '',
          observation: '',
        };
        this.loadData();
        await Swal.fire({
          icon: 'success',
          title: 'Transferencia registrada',
          text: 'El despacho fue registrado y el kardex ya fue actualizado.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo registrar la transferencia.';
        this.cdr.detectChanges();
      },
    });
  }

  private loadData() {
    this.warehousesService.list().subscribe({
      next: (warehouses) => {
        this.warehouses = warehouses;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los almacenes.';
        this.cdr.detectChanges();
      },
    });

    this.productsService.list().subscribe({
      next: (products) => {
        this.products = products;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los productos.';
        this.cdr.detectChanges();
      },
    });

    this.inventoryService.listMovements().subscribe({
      next: (movements) => {
        this.movements = movements;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar las transferencias.';
        this.cdr.detectChanges();
      },
    });
  }

  private getCentralWarehouse(warehouses: Warehouse[]): Warehouse | undefined {
    return [...warehouses].sort((a, b) => {
      const aCentral = a.nombre.toLowerCase().includes('central') ? 1 : 0;
      const bCentral = b.nombre.toLowerCase().includes('central') ? 1 : 0;
      if (aCentral !== bCentral) {
        return bCentral - aCentral;
      }
      return b.capacidad - a.capacidad;
    })[0];
  }
}
