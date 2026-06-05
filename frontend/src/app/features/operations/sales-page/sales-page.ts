import { CommonModule, CurrencyPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SaleSummary, SalesService } from '../../../core/services/sales';

interface TopProduct {
  nombre: string;
  categoria: string;
  unidades: number;
  ingresos: number;
  porcentaje: number;
}

interface SalesTrendItem {
  fecha: string;
  ingresos: number;
  egresos: number;
  utilidad: number;
  canal: string;
  productoMasVendido: string;
  unidades: number;
}

@Component({
  selector: 'app-sales-page',
  imports: [CommonModule, FormsModule, CurrencyPipe],
  templateUrl: './sales-page.html',
  styleUrl: './sales-page.css',
})
export class SalesPage implements OnInit {
  fromDate = '2025-05-01';
  toDate = '2025-05-31';

  summaries: SaleSummary[] = [];
  errorMessage = '';

  constructor(
    private salesService: SalesService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadSales();
  }


  get ingresos() {
    return this.summaries.reduce((acc, item) => acc + Number(item.ingresos || 0), 0);
  }

  get egresos() {
    return this.summaries.reduce((acc, item) => acc + Number(item.egresos || 0), 0);
  }

  get utilidad() {
    return this.ingresos - this.egresos;
  }

  get totalUnidades() {
    return this.summaries.reduce((acc, item) => acc + this.estimatedUnits(item), 0);
  }

  get ticketPromedio() {
    return this.totalUnidades > 0 ? this.ingresos / this.totalUnidades : 0;
  }



  get salesRows(): SalesTrendItem[] {
    return this.summaries.map((item) => ({
      fecha: item.fecha,
      canal: item.canal,
      ingresos: Number(item.ingresos || 0),
      egresos: Number(item.egresos || 0),
      utilidad: Number(item.ingresos || 0) - Number(item.egresos || 0),
      productoMasVendido: item.productoMasVendido,
      unidades: this.estimatedUnits(item),
    }));
  }



  get topProducts(): TopProduct[] {
    const map = new Map<string, TopProduct>();

    for (const item of this.summaries) {
      const nombre = item.productoMasVendido || 'Producto sin nombre';
      const ingresos = Number(item.ingresos || 0);
      const unidades = this.estimatedUnits(item);

      if (!map.has(nombre)) {
        map.set(nombre, {
          nombre,
          categoria: this.productCategory(nombre),
          unidades: 0,
          ingresos: 0,
          porcentaje: 0,
        });
      }

      const product = map.get(nombre)!;
      product.unidades += unidades;
      product.ingresos += ingresos;
    }

    const products = Array.from(map.values())
      .sort((a, b) => b.unidades - a.unidades)
      .slice(0, 5);

    const maxUnits = products.length ? Math.max(...products.map((p) => p.unidades)) : 0;

    return products.map((product) => ({
      ...product,
      porcentaje: maxUnits > 0 ? Math.round((product.unidades / maxUnits) * 100) : 0,
    }));
  }



  get salesTrend(): SalesTrendItem[] {
    return this.salesRows.slice(0, 7);
  }

  get maxChartValue() {
    const values = this.salesTrend.flatMap((item) => [
      item.ingresos,
      item.egresos,
      item.utilidad,
    ]);

    return values.length ? Math.max(...values) : 1;
  }

  chartHeight(value: number) {
    if (!this.maxChartValue) return 0;
    return Math.max(8, Math.round((value / this.maxChartValue) * 100));
  }


  formatMoney(value: number) {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: 'PEN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value || 0);
  }

  formatNumber(value: number) {
    return new Intl.NumberFormat('es-PE').format(value || 0);
  }


  loadSales() {
    this.errorMessage = '';

    this.salesService.list(this.fromDate, this.toDate).subscribe({
      next: (summaries) => {
        this.summaries = summaries || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudo cargar el resumen de ventas.';
        this.cdr.detectChanges();
      },
    });
  }


  private estimatedUnits(item: SaleSummary) {
    const ingresos = Number(item.ingresos || 0);

   
    return Math.max(1, Math.round(ingresos / 100));
  }

  private productCategory(productName: string) {
    const name = productName.toLowerCase();

    if (name.includes('kola') || name.includes('gaseosa') || name.includes('agua')) {
      return 'Bebidas';
    }

    if (name.includes('cheetos') || name.includes('snack')) {
      return 'Snacks';
    }

    if (name.includes('panet') || name.includes('pan')) {
      return 'Panadería';
    }

    if (name.includes('chocol')) {
      return 'Chocolates';
    }

    return 'General';
  }
}