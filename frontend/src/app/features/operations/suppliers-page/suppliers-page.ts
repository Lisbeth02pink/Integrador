import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { Product, ProductsService } from '../../../core/services/products';
import {
  Supplier,
  SupplierDelivery,
  SupplierPayload,
  SunatRucResult,
  SuppliersService,
} from '../../../core/services/suppliers';
import { Warehouse, WarehousesService } from '../../../core/services/warehouses';

@Component({
  selector: 'app-suppliers-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './suppliers-page.html',
  styleUrl: './suppliers-page.css',
})
export class SuppliersPage implements OnInit {
  suppliers: Supplier[] = [];
  deliveries: SupplierDelivery[] = [];
  products: Product[] = [];
  warehouses: Warehouse[] = [];

  modalOpen = false;
  deliveryModalOpen = false;
  editingSupplierId: number | null = null;
  deliverySupplierId: number | null = null;
  saving = false;
  loading = false;
  errorMessage = '';

  // SUNAT lookup state
  sunatLoading = false;
  sunatResult: SunatRucResult | null = null;
  sunatError = '';
  sunatSuccess = false;

  form = this.createEmptyForm();
  deliveryForm = this.createEmptyDeliveryForm();

  constructor(
    private suppliersService: SuppliersService,
    private productsService: ProductsService,
    private warehousesService: WarehousesService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadSuppliers();
  }

  // ── Stats ────────────────────────────────────────────────────────────────────
  get totalSuppliers() { return this.suppliers.length; }
  get activeSuppliers() { return this.suppliers.filter(s => s.estado === 1).length; }
  get inactiveSuppliers() { return this.suppliers.filter(s => s.estado !== 1).length; }

  get centralWarehouse(): Warehouse | undefined {
    return this.getCentralWarehouse(this.warehouses);
  }

  get centralProducts(): Product[] {
    const name = this.centralWarehouse?.nombre;
    return this.products.filter(p => p.almacenNombre === name);
  }

  // ── Modal principal ──────────────────────────────────────────────────────────
  openModal() {
    this.modalOpen = true;
    this.errorMessage = '';
    this.sunatResult = null;
    this.sunatError = '';
    this.sunatSuccess = false;
  }

  closeModal() {
    this.modalOpen = false;
    this.saving = false;
    this.errorMessage = '';
    this.editingSupplierId = null;
    this.sunatResult = null;
    this.sunatError = '';
    this.sunatSuccess = false;
    this.form = this.createEmptyForm();
  }

  // ── Modal entrega ────────────────────────────────────────────────────────────
  openDeliveryModal(supplier: Supplier) {
    this.deliverySupplierId = supplier.id;
    this.deliveryForm = {
      productId: 0,
      warehouseId: this.centralWarehouse?.id ?? 0,
      quantity: 0,
      notes: '',
    };
    this.deliveryModalOpen = true;
    this.errorMessage = '';
  }

  closeDeliveryModal() {
    this.deliveryModalOpen = false;
    this.deliverySupplierId = null;
    this.deliveryForm = this.createEmptyDeliveryForm();
  }

  // ── Editar proveedor ─────────────────────────────────────────────────────────
  editSupplier(supplier: Supplier) {
    this.editingSupplierId = supplier.id;
    this.form = {
      ruc: supplier.ruc,
      razonSocial: supplier.razonSocial,
      contacto: supplier.contacto,
      telefono: supplier.telefono,
      correo: supplier.correo,
      direccion: supplier.direccion,
      productosSuministrados: supplier.productosSuministrados,
      historialEntregas: supplier.historialEntregas,
      activo: supplier.estado === 1,
    };
    this.openModal();
  }

  // ── Consulta SUNAT ───────────────────────────────────────────────────────────
  buscarEnSunat() {
    const ruc = this.form.ruc.trim();
    if (!/^\d{11}$/.test(ruc)) {
      this.sunatError = 'Ingresa un RUC válido de 11 dígitos.';
      this.sunatSuccess = false;
      return;
    }

    this.sunatLoading = true;
    this.sunatError = '';
    this.sunatSuccess = false;
    this.sunatResult = null;
    this.cdr.detectChanges();

    this.suppliersService.lookupSunat(ruc).subscribe({
      next: (data) => {
        this.sunatResult = data;
        this.sunatSuccess = true;
        this.form.razonSocial = data.razonSocial ?? '';
        this.form.direccion = data.direccion ?? '';
        this.sunatLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.sunatError = error?.error?.message || 'No se pudo consultar SUNAT desde el servidor.';
        this.sunatLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  usarDatosSunat() {
    if (!this.sunatResult) return;
    this.form.razonSocial = this.sunatResult.razonSocial ?? this.form.razonSocial;
    this.form.direccion = this.sunatResult.direccion ?? this.form.direccion;
  }

  // ── Toggle / Delete ──────────────────────────────────────────────────────────
  async toggleSupplierStatus(supplier: Supplier) {
    this.suppliersService.toggleStatus(supplier.id).subscribe({
      next: async () => {
        this.loadSuppliers();
        await Swal.fire({ icon: 'success', title: 'Estado actualizado', text: 'El proveedor cambió su estado correctamente.', confirmButtonColor: '#7c3f97' });
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo cambiar el estado del proveedor.';
        this.cdr.detectChanges();
      },
    });
  }

  async deleteSupplier(supplier: Supplier) {
    const result = await Swal.fire({
      icon: 'warning',
      title: `¿Eliminar ${supplier.razonSocial}?`,
      text: 'Esta acción no se puede deshacer.',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#b42318',
    });
    if (!result.isConfirmed) return;

    this.suppliersService.delete(supplier.id).subscribe({
      next: () => this.loadSuppliers(),
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo eliminar el proveedor.';
        this.cdr.detectChanges();
      },
    });
  }

  // ── Guardar proveedor ────────────────────────────────────────────────────────
  saveSupplier() {
    if (!/^\d{11}$/.test(this.form.ruc.trim())) {
      this.errorMessage = 'El RUC debe tener 11 dígitos.';
      return;
    }
    if (!this.form.razonSocial.trim() || !this.form.contacto.trim()) {
      this.errorMessage = 'Completa la razón social y el contacto del proveedor.';
      return;
    }

    this.saving = true;
    this.errorMessage = '';

    const payload: SupplierPayload = {
      ruc: this.form.ruc.trim(),
      razonSocial: this.form.razonSocial.trim().toUpperCase(),
      contacto: this.form.contacto.trim(),
      telefono: this.form.telefono.trim(),
      correo: this.form.correo.trim(),
      direccion: this.form.direccion.trim(),
      productosSuministrados: this.form.productosSuministrados.trim(),
      historialEntregas: this.form.historialEntregas.trim(),
    };

    const request$ = this.editingSupplierId
      ? this.suppliersService.update(this.editingSupplierId, payload)
      : this.suppliersService.create(payload);

    request$.subscribe({
      next: async () => {
        this.loadSuppliers();
        this.closeModal();
        this.cdr.detectChanges();
        await Swal.fire({
          icon: 'success',
          title: this.editingSupplierId ? 'Proveedor actualizado' : 'Proveedor registrado',
          text: 'El proveedor ya quedó conectado al abastecimiento del almacén central.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.saving = false;
        this.errorMessage = error?.error?.message || 'No se pudo guardar el proveedor.';
        this.cdr.detectChanges();
      },
    });
  }

  // ── Guardar entrega ──────────────────────────────────────────────────────────
  saveDelivery() {
    if (!this.deliverySupplierId || !this.deliveryForm.productId || !this.deliveryForm.warehouseId || this.deliveryForm.quantity <= 0) {
      this.errorMessage = 'Completa producto, almacén central y cantidad para registrar la entrega.';
      return;
    }

    this.suppliersService.registerDelivery({
      proveedorId: this.deliverySupplierId,
      productoId: this.deliveryForm.productId,
      almacenDestinoId: this.deliveryForm.warehouseId,
      cantidad: this.deliveryForm.quantity,
      observaciones: this.deliveryForm.notes.trim(),
    }).subscribe({
      next: async () => {
        this.closeDeliveryModal();
        this.loadDeliveries();
        this.loadProducts();
        await Swal.fire({
          icon: 'success',
          title: 'Entrega registrada',
          text: 'El ingreso del proveedor ya aumentó stock y quedó registrado en kardex.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo registrar la entrega del proveedor.';
        this.cdr.detectChanges();
      },
    });
  }

  // ── Loaders privados ─────────────────────────────────────────────────────────
  private loadSuppliers() {
    this.loading = true;

    this.suppliersService.list().subscribe({
      next: (suppliers) => {
        this.suppliers = [...suppliers];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'No se pudieron cargar los proveedores.';
        this.cdr.detectChanges();
      },
    });

    this.loadProducts();

    this.warehousesService.list().subscribe({
      next: (warehouses) => { this.warehouses = warehouses; this.cdr.detectChanges(); },
    });

    this.loadDeliveries();
  }

  private loadDeliveries() {
    this.suppliersService.listDeliveries().subscribe({
      next: (deliveries) => { this.deliveries = deliveries; this.cdr.detectChanges(); },
    });
  }

  private loadProducts() {
    this.productsService.list().subscribe({
      next: (products) => { this.products = products; this.cdr.detectChanges(); },
    });
  }

  private getCentralWarehouse(warehouses: Warehouse[]): Warehouse | undefined {
    return [...warehouses].sort((a, b) => {
      const aCentral = a.nombre.toLowerCase().includes('central') ? 1 : 0;
      const bCentral = b.nombre.toLowerCase().includes('central') ? 1 : 0;
      if (aCentral !== bCentral) return bCentral - aCentral;
      return b.capacidad - a.capacidad;
    })[0];
  }

  private createEmptyForm() {
    return {
      ruc: '',
      razonSocial: '',
      contacto: '',
      telefono: '',
      correo: '',
      direccion: '',
      productosSuministrados: '',
      historialEntregas: '',
      activo: true,
    };
  }

  private createEmptyDeliveryForm() {
    return { productId: 0, warehouseId: 0, quantity: 0, notes: '' };
  }
}
