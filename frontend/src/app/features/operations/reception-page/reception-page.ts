import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { InventoryService, InventoryTransfer } from '../../../core/services/inventory';

@Component({
  selector: 'app-reception-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './reception-page.html',
  styleUrl: './reception-page.css',
})
export class ReceptionPage implements OnInit {
  transfers: InventoryTransfer[] = [];
  errorMessage = '';
  selectedTransferId = 0;
  responsable = '';
  observaciones = '';
  saving = false;

  constructor(
    private inventoryService: InventoryService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadTransfers();
  }

  get pendingReception() {
    return this.transfers.filter((transfer) => transfer.estado === 'ENVIADA');
  }

  get receivedTransfers() {
    return this.transfers.filter((transfer) => transfer.estado === 'RECIBIDA' || transfer.estado === 'COMPLETADA');
  }

  confirmReception() {
    this.errorMessage = '';

    if (!this.selectedTransferId) {
      this.errorMessage = 'Selecciona una transferencia enviada.';
      return;
    }

    this.saving = true;
    this.inventoryService.confirmReception({
      transferenciaId: Number(this.selectedTransferId),
      responsable: this.responsable.trim() || 'Encargado de tienda',
      observaciones: this.observaciones.trim(),
    }).subscribe({
      next: async () => {
        this.selectedTransferId = 0;
        this.responsable = '';
        this.observaciones = '';
        this.saving = false;
        this.loadTransfers();
        await Swal.fire({
          icon: 'success',
          title: 'Recepcion confirmada',
          text: 'La transferencia fue recibida y el stock de tienda fue actualizado.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        this.saving = false;
        this.errorMessage = error?.error?.message || 'No se pudo confirmar la recepcion.';
        this.cdr.detectChanges();
      },
    });
  }

  private loadTransfers() {
    this.inventoryService.listTransfers().subscribe({
      next: (transfers) => {
        this.transfers = transfers;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar las transferencias.';
        this.cdr.detectChanges();
      },
    });
  }
}
