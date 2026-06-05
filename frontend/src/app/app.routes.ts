import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { SidebarLayout } from './features/layout/sidebar-layout/sidebar-layout';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', component: Login },
  {
    path: 'dashboard',
    component: SidebarLayout,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/dashboard/dashboard-home/dashboard-home').then((m) => m.DashboardHome),
      },
      {
        path: 'proveedores',
        loadComponent: () =>
          import('./features/operations/suppliers-page/suppliers-page').then((m) => m.SuppliersPage),
      },
      {
        path: 'asistencia',
        loadComponent: () =>
          import('./features/asistencia/asistencia-page/asistencia-page').then((m) => m.AsistenciaPage),
      },
      {
        path: 'perfiles',
        loadComponent: () =>
          import('./features/perfiles/perfiles-page/perfiles-page').then((m) => m.PerfilesPage),
      },
      {
        path: 'usuarios',
        loadComponent: () =>
          import('./features/usuarios/usuarios-page/usuarios-page').then((m) => m.UsuariosPage),
      },
      {
        path: 'clientes',
        loadComponent: () =>
          import('./features/modules/module-placeholder/module-placeholder').then((m) => m.ModulePlaceholder),
        data: {
          title: 'Gestion de clientes',
          kicker: 'Modulo comercial',
          description: 'Registro y consulta de clientes para ventas y documentos comerciales.',
          highlights: [
            'Clientes con DNI o RUC',
            'Datos de contacto',
            'Estado del cliente',
            'Historial comercial',
          ],
        },
      },
      {
        path: 'categorias',
        loadComponent: () =>
          import('./features/operations/categories-page/categories-page').then((m) => m.CategoriesPage),
      },
      {
        path: 'productos',
        loadComponent: () =>
          import('./features/operations/products-page/products-page').then((m) => m.ProductsPage),
      },
      {
        path: 'ventas',
        loadComponent: () =>
          import('./features/operations/sales-page/sales-page').then((m) => m.SalesPage),
      },
      {
        path: 'inventario',
        loadComponent: () =>
          import('./features/operations/inventory-page/inventory-page').then((m) => m.InventoryPage),
      },
      {
        path: 'almacenes',
        loadComponent: () =>
          import('./features/operations/warehouses-page/warehouses-page').then((m) => m.WarehousesPage),
      },
      {
        path: 'pedidos',
        loadComponent: () =>
          import('./features/operations/internal-orders-page/internal-orders-page').then((m) => m.InternalOrdersPage),
      },
      {
        path: 'tiendas',
        loadComponent: () =>
          import('./features/operations/stores-page/stores-page').then((m) => m.StoresPage),
      },
      {
        path: 'transferencias',
        loadComponent: () =>
          import('./features/operations/transfers-page/transfers-page').then((m) => m.TransfersPage),
      },
      {
        path: 'rutas',
        loadComponent: () =>
          import('./features/operations/routes-page/routes-page').then((m) => m.RoutesPage),
      },
      {
        path: 'recepcion',
        loadComponent: () =>
          import('./features/operations/reception-page/reception-page').then((m) => m.ReceptionPage),
      },
      {
        path: 'mermas',
        loadComponent: () =>
          import('./features/operations/mermas-page/mermas-page').then((m) => m.MermasPage),
      },
      {
        path: 'alertas',
        loadComponent: () =>
          import('./features/operations/alertas-page/alertas-page').then((m) => m.AlertasPage),
      },
      {
        path: 'reportes',
        loadComponent: () =>
          import('./features/operations/reports-page/reports-page').then((m) => m.ReportsPage),
      },
    ],
  },
];
