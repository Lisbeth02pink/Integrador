import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { Merma, MermasService } from '../../../core/services/mermas';
import { Product, ProductsService } from '../../../core/services/products';

@Component({
  selector: 'app-mermas-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './mermas-page.html',
  styleUrl: './mermas-page.css',
})
export class MermasPage implements OnInit {
  products: Product[] = [];
  mermas: Merma[] = [];
  errorMessage = '';
  saving = false;

  form = {
    productoId: 0,
    cantidad: 1,
    motivo: 'Producto danado',
    responsable: '',
    observaciones: '',
  };

  constructor(
    private mermasService: MermasService,
    private productsService: ProductsService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadData();
  }

  get totalMermas() {
    return this.mermas.reduce((acc, item) => acc + item.cantidad, 0);
  }

  registerMerma() {
    this.errorMessage = '';

    if (!this.form.productoId || Number(this.form.cantidad) <= 0 || !this.form.motivo.trim()) {
      this.errorMessage = 'Selecciona producto, cantidad y motivo.';
      return;
    }

    this.saving = true;
    this.mermasService.create({
      productoId: Number(this.form.productoId),
      cantidad: Number(this.form.cantidad),
      motivo: this.form.motivo.trim(),
      responsable: this.form.responsable.trim() || 'Encargado de almacen',
      observaciones: this.form.observaciones.trim(),
    }).subscribe({
      next: async () => {
        this.form = {
          productoId: 0,
          cantidad: 1,
          motivo: 'Producto danado',
          responsable: '',
          observaciones: '',
        };
        this.saving = false;
        this.loadData();
        await Swal.fire({
          icon: 'success',
          title: 'Merma registrada',
          text: 'El stock fue descontado y el movimiento quedo en kardex.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.saving = false;
        this.errorMessage = error?.error?.message || 'No se pudo registrar la merma.';
        this.cdr.detectChanges();
      },
    });
  }

  private loadData() {
    this.productsService.list().subscribe({
      next: (products) => {
        this.products = products;
        this.cdr.detectChanges();
      },
    });

    this.mermasService.list().subscribe({
      next: (mermas) => {
        this.mermas = mermas;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar las mermas.';
        this.cdr.detectChanges();
      },
    });
  }
}
