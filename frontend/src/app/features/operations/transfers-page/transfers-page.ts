import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import Swal from 'sweetalert2';
import { Subscription } from 'rxjs';
import { InventoryMovement, InventoryService } from '../../../core/services/inventory';
import { Product, ProductsService } from '../../../core/services/products';
import { Warehouse, WarehousesService } from '../../../core/services/warehouses';

@Component({
  selector: 'app-transfers-page',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './transfers-page.html',
  styleUrl: './transfers-page.css',
})
export class TransfersPage implements OnInit, OnDestroy {
  movements: InventoryMovement[] = [];
  products: Product[] = [];
  warehouses: Warehouse[] = [];
  errorMessage = '';
  
  transferForm!: FormGroup;
  private subscriptions: Subscription = new Subscription();

  constructor(
    private inventoryService: InventoryService,
    private productsService: ProductsService,
    private warehousesService: WarehousesService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.initForm();
    this.loadData();
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  private initForm() {
    const today = new Date().toISOString().split('T')[0];

    this.transferForm = this.fb.group({
      productId: [0, [Validators.required, Validators.min(1)]],
      destinationWarehouseId: [0, [Validators.required, Validators.min(1)]],
      quantity: [0, [Validators.required, Validators.min(1)]],
      scheduledDate: ['', [Validators.required, this.minDateValidator(today)]],
      observation: ['']
    });

    // Validar cantidad dinámicamente cuando cambie el producto, destino o cantidad
    this.subscriptions.add(
      this.transferForm.valueChanges.subscribe(() => {
        this.validateBusinessRules();
      })
    );
  }

  private minDateValidator(minDate: string) {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      return control.value < minDate ? { minDate: true } : null;
    };
  }

  private validateBusinessRules() {
    const qtyControl = this.transferForm.get('quantity');
    if (!qtyControl) return;

    const qty = Number(qtyControl.value) || 0;
    const prodId = Number(this.transferForm.get('productId')?.value);
    const destId = Number(this.transferForm.get('destinationWarehouseId')?.value);

    let errors: any = null;

    // 1. Validación de stock del producto
    if (prodId > 0) {
      const selectedProduct = this.products.find(p => p.id === prodId);
      if (selectedProduct && qty > selectedProduct.stock) {
        errors = { ...errors, maxStock: { max: selectedProduct.stock, actual: qty } };
      }
    }

    // 2. Validación de capacidad de la tienda destino
    if (destId > 0) {
      const destStore = this.warehouses.find(w => w.id === destId);
      if (destStore) {
        const spaceLeft = destStore.capacidad - destStore.ocupacion;
        if (qty > spaceLeft) {
          errors = { ...errors, maxCapacity: { max: spaceLeft, actual: qty } };
        }
      }
    }

    // Aplicar errores de reglas de negocio al control de cantidad si hay alguno, o combinarlos con errores existentes
    const existingErrors = qtyControl.errors;
    if (errors) {
       qtyControl.setErrors({ ...existingErrors, ...errors });
    } else {
       if (existingErrors) {
          delete existingErrors['maxStock'];
          delete existingErrors['maxCapacity'];
          qtyControl.setErrors(Object.keys(existingErrors).length ? existingErrors : null);
       }
    }
  }

  get centralWarehouse() {
    return this.getCentralWarehouse(this.warehouses);
  }

  get destinationStores() {
    const centralId = this.centralWarehouse?.id;
    return this.warehouses.filter((item) => item.id !== centralId && item.tipo === 'TIENDA');
  }

  get centralProducts() {
    const centralName = this.centralWarehouse?.nombre;
    return this.products.filter((item) => item.almacenNombre === centralName);
  }

  get transferMovements() {
    return this.movements.filter((item) => item.tipo === 'Transferencia');
  }

  get pendingLikeTransfers() {
    return this.transferMovements.filter((item) => item.referencia?.toLowerCase().includes('pedido interno')).length;
  }

  // Getters para UI simplificados
  get isInvalidDate() {
    const control = this.transferForm.get('scheduledDate');
    return control?.touched && control?.errors?.['minDate'];
  }

  get stockError() {
    return this.transferForm.get('quantity')?.errors?.['maxStock'];
  }

  get capacityError() {
    return this.transferForm.get('quantity')?.errors?.['maxCapacity'];
  }

  registerTransfer() {
    this.errorMessage = '';
    
    // Marcar todos como tocados para que aparezcan errores si intenta guardar vacío
    this.transferForm.markAllAsTouched();

    if (this.transferForm.invalid) {
      this.errorMessage = 'Corrige los errores en el formulario antes de guardar.';
      return;
    }

    if (!this.centralWarehouse) {
      this.errorMessage = 'No se encontró un almacén central configurado.';
      return;
    }

    const { productId, destinationWarehouseId, quantity } = this.transferForm.value;

    // Validación de seguridad adicional (Origen !== Destino)
    if (this.centralWarehouse.id === Number(destinationWarehouseId)) {
       this.errorMessage = 'El origen y el destino no pueden ser el mismo almacén.';
       return;
    }

    this.inventoryService.transfer({
      productoId: Number(productId),
      almacenOrigenId: this.centralWarehouse.id,
      almacenDestinoId: Number(destinationWarehouseId),
      cantidad: Number(quantity),
    }).subscribe({
      next: async () => {
        this.transferForm.reset({
          productId: 0,
          destinationWarehouseId: 0,
          quantity: 0,
          scheduledDate: '',
          observation: ''
        });
        
        this.loadData();
        await Swal.fire({
          icon: 'success',
          title: 'Transferencia registrada',
          text: 'El despacho fue registrado y el kardex ya fue actualizado.',
          confirmButtonColor: '#7c3f97',
        });
      },
      error: (error) => {
        let msg = 'No se pudo registrar la transferencia.';
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
    this.warehousesService.list().subscribe({
      next: (warehouses) => {
        this.warehouses = warehouses;
        this.cdr.detectChanges();
        // Disparar validación si cambia capacidad
        this.validateBusinessRules();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los almacenes.';
        this.cdr.detectChanges();
      },
    });

    this.productsService.list().subscribe({
      next: (products) => {
        this.products = products;
        this.cdr.detectChanges();
        // Disparar validación si cambia stock
        this.validateBusinessRules();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los productos.';
        this.cdr.detectChanges();
      },
    });

    this.inventoryService.listMovements().subscribe({
      next: (movements) => {
        this.movements = movements;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar las transferencias.';
        this.cdr.detectChanges();
      },
    });
  }

  private getCentralWarehouse(warehouses: Warehouse[]): Warehouse | undefined {
    return [...warehouses].sort((a, b) => {
      const aCentral = a.nombre.toLowerCase().includes('central') ? 1 : 0;
      const bCentral = b.nombre.toLowerCase().includes('central') ? 1 : 0;
      if (aCentral !== bCentral) {
        return bCentral - aCentral;
      }
      return b.capacidad - a.capacidad;
    })[0];
  }
}
