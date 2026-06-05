import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { Warehouse, WarehousePayload, WarehousesService } from '../../../core/services/warehouses';

// Lista de ciudades y distritos de Perú (principales)
const PERU_CITIES: Record<string, string[]> = {
  Lima: ['Miraflores', 'San Isidro', 'Surco', 'La Molina', 'San Borja', 'Jesús María', 'Lince', 'Chorrillos', 'Villa El Salvador', 'Ate', 'SJL', 'Los Olivos', 'Comas', 'Independencia', 'Breña', 'Centro de Lima'],
  Arequipa: ['Cercado', 'Cayma', 'Cerro Colorado', 'Sachaca', 'Yanahuara', 'Paucarpata', 'Mariano Melgar', 'Hunter'],
  Trujillo: ['Trujillo', 'El Porvenir', 'La Esperanza', 'Florencia de Mora', 'Huanchaco', 'Victor Larco'],
  Chiclayo: ['Chiclayo', 'José Leonardo Ortiz', 'La Victoria', 'Pimentel', 'Monsefú'],
  Piura: ['Piura', 'Castilla', 'Catacaos', 'La Arena', 'Sullana'],
  Cusco: ['Cusco', 'San Jerónimo', 'San Sebastián', 'Santiago', 'Wanchaq'],
  Iquitos: ['Iquitos', 'Belén', 'Punchana', 'San Juan Bautista'],
  Huancayo: ['Huancayo', 'El Tambo', 'Chilca', 'Huancán'],
};

@Component({
  selector: 'app-warehouses-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './warehouses-page.html',
  styleUrl: './warehouses-page.css',
})
export class WarehousesPage implements OnInit {
  warehouses: Warehouse[] = [];
  modalOpen = false;
  editingId: number | null = null;
  errorMessage = '';
  saving = false;
  loading = false;

  readonly cities = Object.keys(PERU_CITIES);
  districts: string[] = [];

  form = this.createEmptyForm();

  constructor(
    private warehousesService: WarehousesService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadWarehouses();
  }

  // ── Stats ────────────────────────────────────────────────────────────────────
  get centralWarehouse(): Warehouse | undefined {
    return this.warehouses.find(w => w.tipo === 'CENTRAL');
  }

  get regionalWarehouses(): Warehouse[] {
    return this.warehouses.filter(w => w.tipo === 'REGIONAL');
  }

  get stores(): Warehouse[] {
    return this.warehouses.filter(w => w.tipo === 'TIENDA');
  }

  get activeWarehouses(): number {
    return this.warehouses.filter(w => w.estado === 1).length;
  }

  get totalWarehouses(): number {
    return this.warehouses.length;
  }

  get averageOccupancy(): number {
    const totalCapacity = this.warehouses.reduce((a, w) => a + w.capacidad, 0);
    const totalOccupation = this.warehouses.reduce((a, w) => a + w.ocupacion, 0);
    return Math.round((totalOccupation / Math.max(totalCapacity, 1)) * 100);
  }

  // ── Modal ────────────────────────────────────────────────────────────────────
  openNewModal() {
    this.editingId = null;
    this.form = this.createEmptyForm();
    this.districts = [];
    this.modalOpen = true;
    this.errorMessage = '';
  }

  editWarehouse(warehouse: Warehouse) {
    this.editingId = warehouse.id;
    this.form = {
      nombre: warehouse.nombre,
      ciudad: warehouse.ciudad,
      distrito: '',
      responsable: warehouse.responsable,
      telefono: '',
      correo: '',
      direccion: warehouse.direccion,
      tipo: warehouse.tipo,
      capacidad: warehouse.capacidad,
      ocupacionPct: Math.round((warehouse.ocupacion / Math.max(warehouse.capacidad, 1)) * 100),
      estado: warehouse.estado === 1 ? 'activo' : 'inactivo',
      observaciones: '',
    };
    this.onCityChange();
    this.modalOpen = true;
    this.errorMessage = '';
  }

  closeModal() {
    this.modalOpen = false;
    this.editingId = null;
    this.errorMessage = '';
    this.saving = false;
    this.form = this.createEmptyForm();
  }

  // ── City/District dynamic ─────────────────────────────────────────────────
  onCityChange() {
    this.districts = PERU_CITIES[this.form.ciudad] ?? [];
    this.form.distrito = '';
  }

  // ── Live preview helpers ──────────────────────────────────────────────────
  get previewOcupacion(): number {
    return Math.round((this.form.ocupacionPct / 100) * this.form.capacidad);
  }

  get tipoLabel(): string {
    return this.typeLabel(this.form.tipo);
  }

  get tipoBadgeClass(): string {
    if (this.form.tipo === 'CENTRAL') return 'wh-badge--central';
    if (this.form.tipo === 'REGIONAL') return 'wh-badge--regional';
    return 'wh-badge--tienda';
  }

  get tipoHint(): string {
    if (this.form.tipo === 'CENTRAL') return 'Este almacén será el nodo principal de abastecimiento.';
    if (this.form.tipo === 'REGIONAL') return 'Distribuye productos a tiendas cercanas.';
    return 'Destino final de distribución y venta.';
  }

  // ── Save ──────────────────────────────────────────────────────────────────
  saveWarehouse() {
    if (!this.form.nombre.trim()) { this.errorMessage = 'Ingresa un nombre válido.'; return; }
    if (!this.form.direccion.trim() || this.form.direccion.trim().length < 8) { this.errorMessage = 'La dirección debe tener al menos 8 caracteres.'; return; }
    if (!this.form.responsable.trim()) { this.errorMessage = 'Asigna un responsable a la sede.'; return; }
    if (this.form.capacidad <= 0) { this.errorMessage = 'La capacidad debe ser mayor a 0.'; return; }
    if (this.previewOcupacion > this.form.capacidad) { this.errorMessage = 'La ocupación no puede exceder la capacidad.'; return; }

    this.saving = true;
    this.errorMessage = '';

    const payload: WarehousePayload = {
      nombre: this.form.nombre.trim(),
      ciudad: this.form.ciudad.trim(),
      responsable: this.form.responsable.trim(),
      direccion: this.form.direccion.trim(),
      tipo: this.form.tipo,
      capacidad: Number(this.form.capacidad),
      ocupacion: this.previewOcupacion,
      estado: this.form.estado === 'activo' ? 1 : 0,
    };

    const request$ = this.editingId
      ? this.warehousesService.update(this.editingId, payload)
      : this.warehousesService.create(payload);

    request$.subscribe({
      next: async () => {
        this.loadWarehouses();
        this.closeModal();
        this.cdr.detectChanges();
        await Swal.fire({
          icon: 'success',
          title: this.editingId ? 'Sede actualizada' : 'Sede registrada correctamente',
          text: 'La sede ya forma parte de la red logística del sistema.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.saving = false;
        this.errorMessage = error?.error?.message || 'No se pudo guardar la sede.';
        this.cdr.detectChanges();
      },
    });
  }

  async deleteWarehouse(warehouse: Warehouse) {
    const result = await Swal.fire({
      icon: 'warning',
      title: `¿Eliminar ${warehouse.nombre}?`,
      text: 'Esta acción no se puede deshacer.',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#b42318',
    });
    if (!result.isConfirmed) return;

    this.warehousesService.delete(warehouse.id).subscribe({
      next: () => this.loadWarehouses(),
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo eliminar la sede.';
        this.cdr.detectChanges();
      },
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────
  occupancyPercent(warehouse: Warehouse): number {
    return Math.min(Math.round((warehouse.ocupacion / Math.max(warehouse.capacidad, 1)) * 100), 100);
  }

  occupancyClass(warehouse: Warehouse): string {
    const pct = this.occupancyPercent(warehouse);
    if (pct >= 85) return 'wh-progress--danger';
    if (pct >= 60) return 'wh-progress--warning';
    return 'wh-progress--ok';
  }

  typeLabel(type: Warehouse['tipo']): string {
    if (type === 'CENTRAL') return 'Central';
    if (type === 'REGIONAL') return 'Regional';
    return 'Tienda';
  }

  private loadWarehouses() {
    this.loading = true;
    this.warehousesService.list().subscribe({
      next: (warehouses) => {
        this.warehouses = warehouses;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'No se pudieron cargar las sedes logísticas.';
        this.cdr.detectChanges();
      },
    });
  }

  private createEmptyForm() {
    return {
      nombre: '',
      ciudad: '',
      distrito: '',
      responsable: '',
      telefono: '',
      correo: '',
      direccion: '',
      tipo: 'TIENDA' as Warehouse['tipo'],
      capacidad: 0,
      ocupacionPct: 0,
      estado: 'activo' as 'activo' | 'inactivo',
      observaciones: '',
    };
  }
}