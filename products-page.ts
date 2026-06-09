import { CommonModule, CurrencyPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { Categoria, CategoriesService } from '../../../core/services/categories';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Product, ProductPayload, ProductsService } from '../../../core/services/products';
import { Warehouse, WarehousesService } from '../../../core/services/warehouses';

@Component({
  selector: 'app-products-page',
  imports: [CommonModule, FormsModule, CurrencyPipe],
  templateUrl: './products-page.html',
  styleUrl: './products-page.css',
})
export class ProductsPage implements OnInit {
  products: Product[] = [];
  lowStockProducts: Product[] = [];
  categories: Categoria[] = [];
  warehouses: Warehouse[] = [];
  modalOpen = false;
  editingId: number | null = null;
  errorMessage = '';
  loading = false;
  saving = false;
  isUploading = false;
  searchTerm = '';
  selectedCategoryFilter = 'all';
  selectedStatusFilter = 'all';
  form = this.createEmptyForm();

  constructor(
    private productsService: ProductsService,
    private categoriesService: CategoriesService,
    private warehousesService: WarehousesService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.loadData();
  }

  get totalActivos() {
    return this.products.filter((item) => item.estado === 1).length;
  }

  get totalValorizado() {
    return this.products.reduce((acc, item) => acc + item.precioCompra * item.stock, 0);
  }

  get totalInactivos() {
    return this.products.filter((item) => item.estado !== 1).length;
  }

  get filteredProducts() {
    const query = this.searchTerm.trim().toLowerCase();

    return this.products.filter((product) => {
      const matchesQuery =
        !query ||
        product.nombre.toLowerCase().includes(query) ||
        product.sku.toLowerCase().includes(query) ||
        product.categoriaNombre.toLowerCase().includes(query) ||
        product.almacenNombre.toLowerCase().includes(query);

      const matchesCategory =
        this.selectedCategoryFilter === 'all' ||
        String(product.categoriaId) === this.selectedCategoryFilter;

      const matchesStatus =
        this.selectedStatusFilter === 'all' ||
        (this.selectedStatusFilter === 'activo' ? product.estado === 1 : product.estado !== 1);

      return matchesQuery && matchesCategory && matchesStatus;
    });
  }

  openNewModal() {
    this.editingId = null;
    this.errorMessage = '';
    this.form = this.createEmptyForm();
    this.modalOpen = true;
  }

  editProduct(product: Product) {
    this.editingId = product.id;
    this.form = {
      nombre: product.nombre,
      sku: product.sku,
      precioCompra: product.precioCompra,
      precioVenta: product.precioVenta,
      stock: product.stock,
      stockMinimo: product.stockMinimo,
      categoriaId: product.categoriaId,
      estado: product.estado === 1 ? 'activo' : 'inactivo',
      imagen: product.imagen,
      almacenId: product.almacenId,
    };
    this.modalOpen = true;
  }

  closeModal() {
    this.modalOpen = false;
    this.editingId = null;
    this.errorMessage = '';
    this.form = this.createEmptyForm();
  }

  saveProduct() {
    if (!this.form.nombre.trim() || !this.form.sku.trim()) {
      this.errorMessage = 'Completa el nombre y el SKU del producto.';
      return;
    }

    if (!this.form.categoriaId || !this.form.almacenId) {
      this.errorMessage = 'Selecciona una categoria y un almacen.';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    const isEditing = this.editingId !== null;

    const payload: ProductPayload = {
      nombre: this.form.nombre.trim(),
      sku: this.form.sku.trim().toUpperCase(),
      precioCompra: Number(this.form.precioCompra),
      precioVenta: Number(this.form.precioVenta),
      stock: Number(this.form.stock),
      stockMinimo: Number(this.form.stockMinimo),
      categoriaId: Number(this.form.categoriaId),
      almacenId: Number(this.form.almacenId),
      imagen: this.form.imagen.trim(),
      estado: this.form.estado === 'activo' ? 1 : 0,
    };

    const request$ = this.editingId
      ? this.productsService.update(this.editingId, payload)
      : this.productsService.create(payload);

    request$.subscribe({
      next: () => {
        this.loadData();
        this.closeModal();
        this.saving = false;
        void Swal.fire({
          icon: 'success',
          title: isEditing ? 'Producto actualizado' : 'Producto registrado',
          text: 'La informacion del producto se guardo correctamente.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.saving = false;
        this.errorMessage = error?.error?.message || 'No se pudo guardar el producto.';
      },
    });
  }

  async deleteProduct(product: Product) {
    const result = await Swal.fire({
      icon: 'warning',
      title: `Eliminar ${product.nombre}?`,
      text: 'Esta accion no se puede deshacer.',
      showCancelButton: true,
      confirmButtonText: 'Si, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#b42318',
    });

    if (!result.isConfirmed) {
      return;
    }

    this.productsService.delete(product.id).subscribe({
      next: () => {
        this.loadData();
        void Swal.fire({
          icon: 'success',
          title: 'Producto eliminado',
          text: 'El producto fue eliminado correctamente.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo eliminar el producto.';
      },
    });
  }

  private loadData() {
    this.loading = true;
    this.categoriesService.list().subscribe({
      next: (data) => {
        this.categories = data;
        this.cdr.detectChanges();
      },
    });
    this.warehousesService.list().subscribe({
      next: (data) => {
        this.warehouses = data;
        this.cdr.detectChanges();
      },
    });
    this.productsService.list().subscribe({
      next: (products) => {
        this.products = products;
        this.lowStockProducts = products.filter((item) => item.stock <= item.stockMinimo);
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'No se pudieron cargar los productos.';
        this.cdr.detectChanges();
      },
    });
  }

  onFileSelected(event: Event) {
    const fileInput = event.target as HTMLInputElement;
    if (fileInput.files && fileInput.files.length > 0) {
      const file = fileInput.files[0];
      const formData = new FormData();
      formData.append('file', file);
      
      this.isUploading = true;
      this.http.post<{url: string}>(`${environment.apiUrl}/upload`, formData).subscribe({
        next: (response) => {
          this.form.imagen = response.url;
          this.isUploading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.errorMessage = 'Error al subir la imagen.';
          this.isUploading = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  private createEmptyForm() {
    return {
      nombre: '',
      sku: '',
      precioCompra: 0,
      precioVenta: 0,
      stock: 0,
      stockMinimo: 0,
      categoriaId: 0,
      estado: 'activo' as 'activo' | 'inactivo',
      imagen: '',
      almacenId: 0,
    };
  }
}
