import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import Swal from 'sweetalert2';
import { TransfersService } from '../../../core/services/transfers';
import { ReceptionService, TransferenciaRecepcion } from '../../../core/services/reception';
import { WarehousesService } from '../../../core/services/warehouses';

@Component({
  selector: 'app-reception-page',
  imports: [CommonModule, ReactiveFormsModule, DatePipe, RouterLink],
  templateUrl: './reception-page.html',
  styleUrl: './reception-page.css',
})
export class ReceptionPage implements OnInit {
  transfers: TransferenciaRecepcion[] = [];
  responsables: string[] = [];
  errorMessage = '';
  saving = false;

  receptionForm: FormGroup;

  constructor(
    private transfersService: TransfersService,
    private receptionService: ReceptionService,
    private warehousesService: WarehousesService,
    private cdr: ChangeDetectorRef,
    private fb: FormBuilder
  ) {
    this.receptionForm = this.fb.group({
      transferenciaId: ['', [Validators.required]],
      responsable: ['', [Validators.required, Validators.minLength(3)]],
      observaciones: ['']
    });
  }

  ngOnInit() {
    this.loadData();
  }

  get inTransitCount() {
    return this.transfers.filter((item) => item.estado === 'En transito').length;
  }

  get completedCount() {
    return this.transfers.filter((item) => item.estado === 'Completado').length;
  }

  isFieldInvalid(field: string): boolean {
    const control = this.receptionForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  confirmReception() {
    this.receptionForm.markAllAsTouched();
    this.errorMessage = '';

    if (this.receptionForm.invalid) {
      this.errorMessage = 'Por favor, selecciona una transferencia y escribe el nombre del responsable.';
      return;
    }

    const formValues = this.receptionForm.value;
    const transferId = Number(formValues.transferenciaId);
    
    const transfer = this.transfers.find((t) => t.id === transferId);

    if (!transfer) {
      this.errorMessage = 'Transferencia no encontrada en la lista.';
      return;
    }
    
    if (transfer.estado === 'Completado') {
      this.errorMessage = 'Esta transferencia ya ha sido recibida anteriormente.';
      return;
    }

    this.saving = true;

    this.receptionService
      .confirmReception({
        transferenciaId: transfer.id,
        responsable: formValues.responsable.trim(),
        observaciones: formValues.observaciones ? formValues.observaciones.trim() : '',
      })
      .subscribe({
        next: async () => {
          this.receptionForm.reset({
            transferenciaId: '',
            responsable: '',
            observaciones: ''
          });
          this.saving = false;
          this.loadData();
          await Swal.fire({
            icon: 'success',
            title: 'Recepción confirmada',
            text: 'El inventario de la tienda ha sido actualizado y la transferencia marcada como completada.',
            confirmButtonColor: '#7c3f97',
          });
        },
        error: (error: any) => {
          this.saving = false;
          let msg = 'No se pudo confirmar la recepción.';
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
    this.transfersService.list().subscribe({
      next: (transfers: TransferenciaRecepcion[]) => {
        this.transfers = transfers.filter((t: TransferenciaRecepcion) => t.estado !== 'Cancelado');
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Error al cargar las transferencias.';
        this.cdr.detectChanges();
      },
    });

    this.warehousesService.list().subscribe({
      next: (warehouses) => {
        const uniqueResponsables = new Set(warehouses.map(w => w.responsable).filter(r => r));
        this.responsables = Array.from(uniqueResponsables);
        this.cdr.detectChanges();
      },
      error: () => {
        console.error('No se pudieron cargar los almacenes para obtener responsables');
      }
    });
  }
}
