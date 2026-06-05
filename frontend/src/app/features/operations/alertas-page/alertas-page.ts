import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Alerta, AlertasService } from '../../../core/services/alertas';

@Component({
  selector: 'app-alertas-page',
  imports: [CommonModule],
  templateUrl: './alertas-page.html',
  styleUrl: './alertas-page.css',
})
export class AlertasPage implements OnInit {
  alertas: Alerta[] = [];
  errorMessage = '';

  constructor(
    private alertasService: AlertasService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadAlerts();
  }

  countBy(severidad: Alerta['severidad']) {
    return this.alertas.filter((alerta) => alerta.severidad === severidad).length;
  }

  private loadAlerts() {
    this.alertasService.list().subscribe({
      next: (alertas) => {
        this.alertas = alertas;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar las alertas logisticas.';
        this.cdr.detectChanges();
      },
    });
  }
}
