import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { Warehouse, WarehousePayload, WarehousesService } from '../../../core/services/warehouses';

@Component({
  selector: 'app-stores-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './stores-page.html',
  styleUrl: './stores-page.css',
})
export class StoresPage implements OnInit {
  stores: Warehouse[] = [];
  modalOpen = false;
  editingId: number | null = null;
  errorMessage = '';
  form = this.createEmptyForm();

  constructor(
    private warehousesService: WarehousesService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadStores();
  }

  get totalActiveStores() {
    return this.stores.filter((item) => item.estado === 1).length;
  }

  get totalCapacity() {
    return this.stores.reduce((acc, item) => acc + item.capacidad, 0);
  }

  openNewModal() {
    this.editingId = null;
    this.errorMessage = '';
    this.form = this.createEmptyForm();
    this.modalOpen = true;
  }

  editStore(store: Warehouse) {
    this.editingId = store.id;
    this.form = {
      nombre: store.nombre,
      ciudad: store.ciudad,
      responsable: store.responsable,
      direccion: store.direccion,
      tipo: 'TIENDA' as 'TIENDA',
      capacidad: store.capacidad,
      ocupacion: store.ocupacion,
      estado: store.estado === 1 ? 'activo' : 'inactivo',
    };
    this.modalOpen = true;
  }

  closeModal() {
    this.modalOpen = false;
    this.editingId = null;
    this.errorMessage = '';
    this.form = this.createEmptyForm();
  }

  saveStore() {
    if (!this.form.nombre.trim() || !this.form.responsable.trim()) {
      this.errorMessage = 'Completa el nombre de la tienda y su responsable.';
      return;
    }

    const payload: WarehousePayload = {
      nombre: this.normalizeStoreName(this.form.nombre),
      ciudad: this.form.ciudad.trim(),
      responsable: this.form.responsable.trim(),
      direccion: this.form.direccion.trim(),
      tipo: 'TIENDA',
      capacidad: Number(this.form.capacidad),
      ocupacion: Number(this.form.ocupacion),
      estado: this.form.estado === 'activo' ? 1 : 0,
    };

    const request$ = this.editingId
      ? this.warehousesService.update(this.editingId, payload)
      : this.warehousesService.create(payload);

    request$.subscribe({
      next: async () => {
        this.loadStores();
        this.closeModal();
        this.cdr.detectChanges();
        await Swal.fire({
          icon: 'success',
          title: this.editingId ? 'Tienda actualizada' : 'Tienda registrada',
          text: 'La tienda quedo vinculada al circuito de abastecimiento.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo guardar la tienda.';
        this.cdr.detectChanges();
      },
    });
  }

  async deleteStore(store: Warehouse) {
    const result = await Swal.fire({
      icon: 'warning',
      title: `Eliminar ${store.nombre}?`,
      text: 'Esta accion no se puede deshacer.',
      showCancelButton: true,
      confirmButtonText: 'Si, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#b42318',
    });

    if (!result.isConfirmed) {
      return;
    }

    this.warehousesService.delete(store.id).subscribe({
      next: () => this.loadStores(),
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo eliminar la tienda.';
        this.cdr.detectChanges();
      },
    });
  }

  private loadStores() {
    this.warehousesService.list().subscribe({
      next: (warehouses) => {
        this.stores = this.filterStores(warehouses);
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar las tiendas.';
        this.cdr.detectChanges();
      },
    });
  }

  private filterStores(warehouses: Warehouse[]) {
    return warehouses.filter((item) => item.tipo === 'TIENDA');
  }

  private normalizeStoreName(name: string) {
    const trimmed = name.trim();
    return trimmed.toLowerCase().startsWith('tienda tambo') ? trimmed : `Tienda Tambo ${trimmed}`;
  }

  private createEmptyForm() {
    return {
      nombre: '',
      ciudad: '',
      responsable: '',
      direccion: '',
      tipo: 'TIENDA' as 'TIENDA',
      capacidad: 0,
      ocupacion: 0,
      estado: 'activo' as 'activo' | 'inactivo',
    };
  }
}
