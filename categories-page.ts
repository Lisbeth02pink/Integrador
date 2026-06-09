import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Categoria, CategoriaPayload, CategoriesService } from '../../../core/services/categories';

@Component({
  selector: 'app-categories-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './categories-page.html',
  styleUrl: './categories-page.css',
})
export class CategoriesPage implements OnInit {
  categories: Categoria[] = [];
  modalOpen = false;
  editingId: number | null = null;
  errorMessage = '';
  loading = false;
  saving = false;
  isUploading = false;

  form = this.createEmptyForm();

  constructor(
    private categoriesService: CategoriesService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.loadCategories();
  }

  openNewModal() {
    this.editingId = null;
    this.form = this.createEmptyForm();
    this.modalOpen = true;
  }

  editCategory(category: Categoria) {
    this.editingId = category.id;
    this.form = {
      nombre: category.nombre,
      codigo: category.codigo,
      descripcion: category.descripcion,
      imagen: category.imagen,
      estado: category.estado === 1 ? 'activo' : 'inactivo',
    };
    this.modalOpen = true;
  }

  closeModal() {
    this.modalOpen = false;
    this.editingId = null;
    this.errorMessage = '';
    this.form = this.createEmptyForm();
  }

  saveCategory() {
    if (!this.form.nombre.trim() || !this.form.codigo.trim()) {
      this.errorMessage = 'Completa el nombre y el codigo de la categoria.';
      return;
    }

    this.saving = true;
    this.errorMessage = '';

    const isEditing = this.editingId !== null;
    const payload: CategoriaPayload = {
      nombre: this.form.nombre.trim(),
      codigo: this.form.codigo.trim().toUpperCase(),
      descripcion: this.form.descripcion.trim(),
      imagen: this.form.imagen.trim(),
      estado: this.form.estado === 'activo' ? 1 : 0,
    };

    const request$ = this.editingId
      ? this.categoriesService.update(this.editingId, payload)
      : this.categoriesService.create(payload);

    request$.subscribe({
      next: () => {
        this.loadCategories();
        this.closeModal();
        this.saving = false;
        void Swal.fire({
          icon: 'success',
          title: isEditing ? 'Categoria actualizada' : 'Categoria registrada',
          text: 'Los cambios se guardaron correctamente.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.saving = false;
        this.errorMessage = error?.error?.message || 'No se pudo guardar la categoria.';
      },
    });
  }

  async deleteCategory(category: Categoria) {
    const result = await Swal.fire({
      icon: 'warning',
      title: `Eliminar ${category.nombre}?`,
      text: 'Esta accion no se puede deshacer.',
      showCancelButton: true,
      confirmButtonText: 'Si, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#b42318',
    });

    if (!result.isConfirmed) {
      return;
    }

    this.categoriesService.delete(category.id).subscribe({
      next: () => {
        this.loadCategories();
        void Swal.fire({
          icon: 'success',
          title: 'Categoria eliminada',
          text: 'La categoria fue eliminada correctamente.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'No se pudo eliminar la categoria.';
      },
    });
  }

  private loadCategories() {
    this.loading = true;
    this.categoriesService.list().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'No se pudieron cargar las categorias.';
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
      codigo: '',
      descripcion: '',
      imagen: '',
      estado: 'activo' as 'activo' | 'inactivo',
    };
  }
}
