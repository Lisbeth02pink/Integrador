import { Injectable } from '@angular/core';

export interface CategoryItem {
  id: number;
  nombre: string;
  codigo: string;
  descripcion: string;
  imagen: string;
  estado: 'activo' | 'inactivo';
}

export interface ProductItem {
  id: number;
  nombre: string;
  sku: string;
  precioCompra: number;
  precioVenta: number;
  stock: number;
  stockMinimo: number;
  categoriaId: number;
  categoriaNombre: string;
  estado: 'activo' | 'inactivo';
  imagen: string;
  almacenId: number;
  almacenNombre: string;
}

export interface WarehouseItem {
  id: number;
  nombre: string;
  ciudad: string;
  responsable: string;
  direccion: string;
  capacidad: number;
  ocupacion: number;
  estado: 'activo' | 'inactivo';
}

export interface InventoryMovement {
  id: number;
  fecha: string;
  productoSku: string;
  productoNombre: string;
  tipo: 'Ingreso' | 'Egreso' | 'Transferencia';
  cantidad: number;
  almacenOrigen?: string;
  almacenDestino?: string;
  referencia: string;
}

export interface DeliveryRouteItem {
  id: number;
  nombre: string;
  zona: string;
  repartidor: string;
  pedidos: number;
  estado: 'Pendiente' | 'En ruta' | 'Entregado';
  horaSalida: string;
  progreso: number;
}

export interface EmployeeControlItem {
  id: number;
  nombre: string;
  cargo: string;
  entrada: string;
  salida: string;
  tardanzas: number;
  faltas: number;
  asistencias: number;
  estado: 'Presente' | 'Tarde' | 'Falta';
}

@Injectable({ providedIn: 'root' })
export class OperationsDataService {
  private categories: CategoryItem[] = [
    {
      id: 1,
      nombre: 'Bebidas',
      codigo: 'CAT-BEB',
      descripcion: 'Gaseosas, aguas y bebidas energizantes.',
      imagen: 'https://images.unsplash.com/photo-1544145945-f90425340c7e?auto=format&fit=crop&w=500&q=80',
      estado: 'activo',
    },
    {
      id: 2,
      nombre: 'Snacks',
      codigo: 'CAT-SNK',
      descripcion: 'Galletas, golosinas y snacks salados.',
      imagen: 'https://images.unsplash.com/photo-1599490659213-e2b9527bd087?auto=format&fit=crop&w=500&q=80',
      estado: 'activo',
    },
    {
      id: 3,
      nombre: 'Limpieza',
      codigo: 'CAT-LIM',
      descripcion: 'Papel higienico, limpieza y cuidado del punto de venta.',
      imagen: 'https://images.unsplash.com/photo-1583947582886-f40ec95dd752?auto=format&fit=crop&w=500&q=80',
      estado: 'activo',
    },
  ];

  private warehouses: WarehouseItem[] = [
    {
      id: 1,
      nombre: 'Almacen Lima',
      ciudad: 'Lima',
      responsable: 'Rosa Calderon',
      direccion: 'Av. Javier Prado 1480',
      capacidad: 1000,
      ocupacion: 760,
      estado: 'activo',
    },
    {
      id: 2,
      nombre: 'Almacen Chiclayo',
      ciudad: 'Chiclayo',
      responsable: 'Luis Paredes',
      direccion: 'Av. Balta 922',
      capacidad: 640,
      ocupacion: 420,
      estado: 'activo',
    },
    {
      id: 3,
      nombre: 'Almacen Trujillo',
      ciudad: 'Trujillo',
      responsable: 'Mila Torres',
      direccion: 'Jr. Pizarro 311',
      capacidad: 520,
      ocupacion: 178,
      estado: 'activo',
    },
  ];

  private products: ProductItem[] = [
    {
      id: 1,
      nombre: 'Agua Mineral 625ml',
      sku: 'BEB-001',
      precioCompra: 1.1,
      precioVenta: 2.5,
      stock: 18,
      stockMinimo: 20,
      categoriaId: 1,
      categoriaNombre: 'Bebidas',
      estado: 'activo',
      imagen: 'https://images.unsplash.com/photo-1564419434663-c499673ea48f?auto=format&fit=crop&w=400&q=80',
      almacenId: 1,
      almacenNombre: 'Almacen Lima',
    },
    {
      id: 2,
      nombre: 'Inka Kola 500ml',
      sku: 'SNK-104',
      precioCompra: 2,
      precioVenta: 3.5,
      stock: 42,
      stockMinimo: 12,
      categoriaId: 1,
      categoriaNombre: 'Bebidas',
      estado: 'activo',
      imagen: 'https://images.unsplash.com/photo-1629203851122-3726ecdf080e?auto=format&fit=crop&w=400&q=80',
      almacenId: 1,
      almacenNombre: 'Almacen Lima',
    },
    {
      id: 3,
      nombre: 'Papel Higienico Elite 4 und',
      sku: 'LIM-205',
      precioCompra: 8.5,
      precioVenta: 12.9,
      stock: 27,
      stockMinimo: 10,
      categoriaId: 3,
      categoriaNombre: 'Limpieza',
      estado: 'activo',
      imagen: 'https://images.unsplash.com/photo-1604335399105-a0c585fd81a1?auto=format&fit=crop&w=400&q=80',
      almacenId: 2,
      almacenNombre: 'Almacen Chiclayo',
    },
    {
      id: 4,
      nombre: 'Galletas Oreo Clasicas',
      sku: 'SNK-222',
      precioCompra: 1.8,
      precioVenta: 2.8,
      stock: 64,
      stockMinimo: 18,
      categoriaId: 2,
      categoriaNombre: 'Snacks',
      estado: 'activo',
      imagen: 'https://images.unsplash.com/photo-1558961363-fa8fdf82db35?auto=format&fit=crop&w=400&q=80',
      almacenId: 3,
      almacenNombre: 'Almacen Trujillo',
    },
  ];

  private movements: InventoryMovement[] = [
    {
      id: 1,
      fecha: '2026-04-25 08:10',
      productoSku: 'BEB-001',
      productoNombre: 'Agua Mineral 625ml',
      tipo: 'Egreso',
      cantidad: 12,
      almacenOrigen: 'Almacen Lima',
      referencia: 'Venta mostrador VTA-1802',
    },
    {
      id: 2,
      fecha: '2026-04-24 17:42',
      productoSku: 'SNK-104',
      productoNombre: 'Inka Kola 500ml',
      tipo: 'Ingreso',
      cantidad: 15,
      almacenDestino: 'Almacen Lima',
      referencia: 'Compra proveedor OC-903',
    },
    {
      id: 3,
      fecha: '2026-04-24 13:15',
      productoSku: 'LIM-205',
      productoNombre: 'Papel Higienico Elite 4 und',
      tipo: 'Transferencia',
      cantidad: 8,
      almacenOrigen: 'Almacen Lima',
      almacenDestino: 'Almacen Chiclayo',
      referencia: 'Transferencia TR-101',
    },
    {
      id: 4,
      fecha: '2026-04-23 09:00',
      productoSku: 'SNK-222',
      productoNombre: 'Galletas Oreo Clasicas',
      tipo: 'Egreso',
      cantidad: 7,
      almacenOrigen: 'Almacen Trujillo',
      referencia: 'Pedido ecommerce PED-782',
    },
  ];

  private routes: DeliveryRouteItem[] = [
    {
      id: 1,
      nombre: 'Ruta Norte 01',
      zona: 'Los Olivos - SMP',
      repartidor: 'Jorge Meza',
      pedidos: 13,
      estado: 'En ruta',
      horaSalida: '08:20',
      progreso: 58,
    },
    {
      id: 2,
      nombre: 'Ruta Centro 03',
      zona: 'Lince - Jesus Maria',
      repartidor: 'Camila Vela',
      pedidos: 8,
      estado: 'Pendiente',
      horaSalida: '10:00',
      progreso: 14,
    },
    {
      id: 3,
      nombre: 'Ruta Sur 02',
      zona: 'Surco - Chorrillos',
      repartidor: 'Marco Ponce',
      pedidos: 17,
      estado: 'Entregado',
      horaSalida: '07:45',
      progreso: 100,
    },
  ];

  private employeeControl: EmployeeControlItem[] = [
    {
      id: 1,
      nombre: 'Ana Delgado',
      cargo: 'Cajera',
      entrada: '08:02',
      salida: '17:00',
      tardanzas: 1,
      faltas: 0,
      asistencias: 24,
      estado: 'Presente',
    },
    {
      id: 2,
      nombre: 'Piero Salas',
      cargo: 'Almacenero',
      entrada: '08:21',
      salida: '17:00',
      tardanzas: 4,
      faltas: 1,
      asistencias: 21,
      estado: 'Tarde',
    },
    {
      id: 3,
      nombre: 'Diana Flores',
      cargo: 'Repartidora',
      entrada: '--',
      salida: '--',
      tardanzas: 0,
      faltas: 2,
      asistencias: 20,
      estado: 'Falta',
    },
  ];

  listCategories() {
    return [...this.categories];
  }

  saveCategory(payload: Omit<CategoryItem, 'id'>, id?: number) {
    if (id) {
      this.categories = this.categories.map((item) => (item.id === id ? { ...item, ...payload } : item));
      return;
    }

    this.categories = [
      { id: this.nextId(this.categories), ...payload },
      ...this.categories,
    ];
  }

  deleteCategory(id: number) {
    this.categories = this.categories.filter((item) => item.id !== id);
  }

  listProducts() {
    return [...this.products];
  }

  saveProduct(payload: Omit<ProductItem, 'id'>, id?: number) {
    const normalized = {
      ...payload,
      categoriaNombre: this.categories.find((item) => item.id === payload.categoriaId)?.nombre ?? payload.categoriaNombre,
      almacenNombre: this.warehouses.find((item) => item.id === payload.almacenId)?.nombre ?? payload.almacenNombre,
    };

    if (id) {
      this.products = this.products.map((item) => (item.id === id ? { ...item, ...normalized } : item));
      return;
    }

    this.products = [{ id: this.nextId(this.products), ...normalized }, ...this.products];
  }

  deleteProduct(id: number) {
    this.products = this.products.filter((item) => item.id !== id);
  }

  listWarehouses() {
    return [...this.warehouses];
  }

  saveWarehouse(payload: Omit<WarehouseItem, 'id'>, id?: number) {
    if (id) {
      this.warehouses = this.warehouses.map((item) => (item.id === id ? { ...item, ...payload } : item));
      return;
    }

    this.warehouses = [{ id: this.nextId(this.warehouses), ...payload }, ...this.warehouses];
  }

  deleteWarehouse(id: number) {
    this.warehouses = this.warehouses.filter((item) => item.id !== id);
  }

  listMovements() {
    return [...this.movements];
  }

  registerTransfer(productId: number, originWarehouseId: number, destinationWarehouseId: number, quantity: number) {
    const product = this.products.find((item) => item.id === productId);
    const origin = this.warehouses.find((item) => item.id === originWarehouseId);
    const destination = this.warehouses.find((item) => item.id === destinationWarehouseId);

    if (!product || !origin || !destination || quantity <= 0) {
      return;
    }

    if (product.stock < quantity) {
      return;
    }

    this.products = this.products.map((item) =>
      item.id === productId
        ? {
            ...item,
            stock: item.stock - quantity,
            almacenId: destination.id,
            almacenNombre: destination.nombre,
          }
        : item
    );

    this.movements = [
      {
        id: this.nextId(this.movements),
        fecha: '2026-04-25 11:30',
        productoSku: product.sku,
        productoNombre: product.nombre,
        tipo: 'Transferencia',
        cantidad: quantity,
        almacenOrigen: origin.nombre,
        almacenDestino: destination.nombre,
        referencia: `Transferencia ${origin.ciudad} a ${destination.ciudad}`,
      },
      ...this.movements,
    ];
  }

  listRoutes() {
    return [...this.routes];
  }

  saveRoute(payload: Omit<DeliveryRouteItem, 'id'>, id?: number) {
    if (id) {
      this.routes = this.routes.map((item) => (item.id === id ? { ...item, ...payload } : item));
      return;
    }

    this.routes = [{ id: this.nextId(this.routes), ...payload }, ...this.routes];
  }

  deleteRoute(id: number) {
    this.routes = this.routes.filter((item) => item.id !== id);
  }

  listEmployeeControl() {
    return [...this.employeeControl];
  }

  getLowStockProducts() {
    return this.products.filter((item) => item.stock <= item.stockMinimo);
  }

  getActiveProductsByWarehouse() {
    return this.warehouses.map((warehouse) => ({
      warehouse,
      totalProductos: this.products.filter((item) => item.almacenId === warehouse.id).length,
      stockTotal: this.products
        .filter((item) => item.almacenId === warehouse.id)
        .reduce((acc, item) => acc + item.stock, 0),
    }));
  }

  private nextId<T extends { id: number }>(collection: T[]) {
    return collection.length ? Math.max(...collection.map((item) => item.id)) + 1 : 1;
  }
}
