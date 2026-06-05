import { CommonModule } from '@angular/common';
import { Component, HostListener, OnInit } from '@angular/core';
import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';

import { Auth } from '../../../core/services/auth';
import {
  ModuleItem,
  ProfilesService,
} from '../../../core/services/profiles';

import { AppIconComponent } from '../../../shared/components/app-icon/app-icon';

interface SidebarItem {
  moduleName: string;
  label: string;
  icon: string;
  route?: string;
}

interface SidebarGroup {
  title: string;
  expanded: boolean;
  items: SidebarItem[];
}

@Component({
  selector: 'app-sidebar-layout',
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    AppIconComponent,
  ],
  templateUrl: './sidebar-layout.html',
  styleUrl: './sidebar-layout.css',
})
export class SidebarLayout implements OnInit {

  private readonly desktopBreakpoint = 768;

  private readonly allGroups: SidebarGroup[] = [
    {
      title: 'General',
      expanded: true,
      items: [
        {
          moduleName: 'Dashboard',
          label: 'Dashboard',
          icon: 'dashboard',
          route: '/dashboard',
        },
        {
          moduleName: 'Gestion Asistencia',
          label: 'Asistencia',
          icon: 'fingerprint',
          route: '/dashboard/asistencia',
        },
      ],
    },

    {
      title: 'Logistica',
      expanded: true,
      items: [
        {
          moduleName: 'Gestion Proveedores',
          label: 'Proveedores',
          icon: 'handshake',
          route: '/dashboard/proveedores',
        },
        {
          moduleName: 'Gestion Almacenes',
          label: 'Almacenes / Tiendas',
          icon: 'warehouse',
          route: '/dashboard/almacenes',
        },
        {
          moduleName: 'Inventario por Ubicacion',
          label: 'Inventario',
          icon: 'boxes',
          route: '/dashboard/inventario',
        },
        {
          moduleName: 'Pedidos Internos',
          label: 'Pedidos internos',
          icon: 'clipboard-list',
          route: '/dashboard/pedidos',
        },
        {
          moduleName: 'Transferencias Logisticas',
          label: 'Transferencias',
          icon: 'arrow-right-left',
          route: '/dashboard/transferencias',
        },
        {
          moduleName: 'Distribucion y Rutas',
          label: 'Distribucion y rutas',
          icon: 'truck',
          route: '/dashboard/rutas',
        },
        {
          moduleName: 'Recepcion en Tienda',
          label: 'Recepcion en tienda',
          icon: 'clipboard-check',
          route: '/dashboard/recepcion',
        },
        {
          moduleName: 'Mermas y Danados',
          label: 'Mermas',
          icon: 'alert-triangle',
          route: '/dashboard/mermas',
        },
        {
          moduleName: 'Alertas Logisticas',
          label: 'Alertas',
          icon: 'bell',
          route: '/dashboard/alertas',
        },
      ],
    },

    {
      title: 'Comercial',
      expanded: true,
      items: [
        {
          moduleName: 'Gestion Categorias',
          label: 'Categorias',
          icon: 'tag',
          route: '/dashboard/categorias',
        },
        {
          moduleName: 'Gestion Productos',
          label: 'Productos',
          icon: 'package',
          route: '/dashboard/productos',
        },
        {
          moduleName: 'Gestion Ventas',
          label: 'Ventas',
          icon: 'shopping-cart',
          route: '/dashboard/ventas',
        },
        {
          moduleName: 'Gestion Reportes',
          label: 'Reportes',
          icon: 'bar-chart',
          route: '/dashboard/reportes',
        },
      ],
    },

    {
      title: 'Administracion',
      expanded: true,
      items: [
        {
          moduleName: 'Gestion Perfiles',
          label: 'Roles',
          icon: 'shield-check',
          route: '/dashboard/perfiles',
        },
        {
          moduleName: 'Gestion Usuarios',
          label: 'Usuarios',
          icon: 'users',
          route: '/dashboard/usuarios',
        },
      ],
    },
  ];

  groups: SidebarGroup[] = [];

  isSidebarOpen = true;
  isDesktop = true;

  currentUserName = 'Administrador';
  currentUserEmail = 'admin@tambo.com';
  userInitials = 'AD';

  constructor(
    private authService: Auth,
    private profilesService: ProfilesService,
    private router: Router
  ) {}

  @HostListener('window:resize')
onResize() {

  this.isDesktop =
    window.innerWidth > this.desktopBreakpoint;

  if (this.isDesktop) {

    // DESKTOP
    this.isSidebarOpen = true;

  } else {

    // MOBILE
    this.isSidebarOpen = false;
  }
}

  ngOnInit() {

    this.isDesktop = window.innerWidth > this.desktopBreakpoint;
    this.isSidebarOpen = this.isDesktop;

    const session = this.authService.getSession();

    this.currentUserName =
      session?.nombre ||
      session?.usuario ||
      'Administrador';

    this.currentUserEmail =
      session?.correo ||
      'admin@tambo.com';

    this.userInitials = this.getInitials(
      this.currentUserName
    );

    const allowedModules = new Set(
      session?.modulos ?? []
    );

    const baseGroups =
      allowedModules.size === 0
        ? this.allGroups
        : this.allGroups
            .map((group) => ({
              ...group,
              items: group.items.filter(
                (item) =>
                  item.moduleName === 'Dashboard' ||
                  allowedModules.has(item.moduleName)
              ),
            }))
            .filter((group) => group.items.length > 0);

    this.groups = baseGroups;

    const cachedModules =
      this.profilesService.getModulesSnapshot();

    if (cachedModules?.length) {
      this.groups = this.applyModuleIcons(
        baseGroups,
        cachedModules
      );
    }
  }

  toggleSidebar() {

  // desktop → colapsa/expande
  if (this.isDesktop) {
    this.isSidebarOpen = !this.isSidebarOpen;
    return;
  }

  // mobile → abre/cierra overlay
  this.isSidebarOpen = !this.isSidebarOpen;
}

  toggleGroup(group: SidebarGroup) {
    group.expanded = !group.expanded;
  }

  trackByTitle(_: number, group: SidebarGroup) {
    return group.title;
  }

  trackByLabel(_: number, item: SidebarItem) {
    return item.moduleName;
  }

  isImageIcon(icon: string) {
    const normalized = icon.trim().toLowerCase();

    return (
      normalized.startsWith('http://') ||
      normalized.startsWith('https://') ||
      normalized.startsWith('assets/') ||
      normalized.endsWith('.svg') ||
      normalized.endsWith('.png') ||
      normalized.endsWith('.jpg') ||
      normalized.endsWith('.jpeg') ||
      normalized.endsWith('.webp')
    );
  }

  logout() {

    const refreshToken =
      this.authService.getRefreshToken();

    if (!refreshToken) {
      this.authService.clearSession();
      this.router.navigate(['/']);
      return;
    }

    this.authService.logout(refreshToken).subscribe({
      next: () => {
        this.authService.clearSession();
        this.router.navigate(['/']);
      },

      error: () => {
        this.authService.clearSession();
        this.router.navigate(['/']);
      },
    });
  }

  onNavigate() {

    if (!this.isDesktop) {
      this.isSidebarOpen = false;
    }
  }

  get currentRouteLabel() {

    const current = this.groups
      .flatMap((group) => group.items)
      .find((item) => item.route === this.router.url);

    return current?.label ?? 'Dashboard';
  }

  private getInitials(name: string): string {

    return name
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) =>
        part.charAt(0).toUpperCase()
      )
      .join('');
  }

  private applyModuleIcons(
    groups: SidebarGroup[],
    modules: ModuleItem[]
  ): SidebarGroup[] {

    const moduleMap = new Map(
      modules.map((module) => [
        module.nombre,
        module,
      ])
    );

    return groups.map((group) => ({
      ...group,

      items: group.items.map((item) => {

        const module = moduleMap.get(
          item.moduleName
        );

        return {
          ...item,
          route: item.route || module?.ruta,
        };
      }),
    }));
  }
}
