import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { rolGuard } from './core/guards/rol-guard';

export const routes: Routes = [
  {
    path: 'auth/callback',
    loadComponent: () => import('./core/auth/auth-callback').then((m) => m.AuthCallback),
    title: 'Autenticación · ERP Agro Peruvian Foods',
  },
  {
    path: '',
    loadComponent: () => import('./core/layout/layout').then((m) => m.Layout),
    children: [
      {
        path: '',
        loadComponent: () => import('./core/home/home').then((m) => m.Home),
        title: 'Dashboard · ERP Agro Peruvian Foods',
      },
      // Módulo 1: Finanzas & Caja (Brandon Ccalla)
      {
        path: 'caja',
        canActivate: [authGuard, rolGuard('SUPERVISOR', 'ADMIN')],
        loadComponent: () => import('./features/caja/caja-dashboard').then((m) => m.CajaDashboard),
        title: 'Caja & Movimientos · ERP Agro Peruvian Foods',
      },
      // Módulo 2: Ventas POS (Zaggy Morales) - S09
      {
        path: 'ventas',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/ventas/venta-reporte').then((m) => m.VentaReporteComponent),
        title: 'Reporte de Ventas POS (S09) · ERP Agro Peruvian Foods',
      },
      {
        path: 'ventas/reporte',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/ventas/venta-reporte').then((m) => m.VentaReporteComponent),
        title: 'Reporte de Ventas POS (S09) · ERP Agro Peruvian Foods',
      },
      {
        path: 'ventas/nueva',
        canActivate: [authGuard, rolGuard('VENDEDOR', 'SUPERVISOR')],
        loadComponent: () => import('./features/ventas/venta-form').then((m) => m.VentaForm),
        title: 'Nueva Venta POS (S09) · ERP Agro Peruvian Foods',
      },
      // Módulo 3: Producción & BOM (Isaí Armuto) + Catálogo S07 & S08
      {
        path: 'produccion',
        canActivate: [authGuard, rolGuard('SUPERVISOR', 'ADMIN')],
        loadComponent: () => import('./features/produccion/orden-list').then((m) => m.OrdenList),
        title: 'Órdenes de Producción BOM · ERP Agro Peruvian Foods',
      },
      {
        path: 'produccion/nueva',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () => import('./features/produccion/orden-form').then((m) => m.OrdenForm),
        title: 'Nueva Orden de Trabajo · ERP Agro Peruvian Foods',
      },
      // Catálogo S07 (Categorías)
      {
        path: 'catalogo/categorias',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-list').then((m) => m.CategoriaList),
        title: 'Categorías (S07) · ERP Agro Peruvian Foods',
      },
      {
        path: 'catalogo/categorias/nueva',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-form').then((m) => m.CategoriaForm),
        title: 'Nueva Categoría · ERP Agro Peruvian Foods',
      },
      {
        path: 'catalogo/categorias/:id',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-form').then((m) => m.CategoriaForm),
        title: 'Editar Categoría · ERP Agro Peruvian Foods',
      },
      // Catálogo S08 (Productos)
      {
        path: 'catalogo/productos',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-list').then((m) => m.ProductoList),
        title: 'Catálogo de Productos (S08) · ERP Agro Peruvian Foods',
      },
      {
        path: 'catalogo/productos/nuevo',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
        title: 'Nuevo Producto (S08) · ERP Agro Peruvian Foods',
      },
      {
        path: 'catalogo/productos/:id/editar',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
        title: 'Editar Producto (S08) · ERP Agro Peruvian Foods',
      },
      {
        path: 'catalogo/productos/:id',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
        title: 'Ver/Editar Producto (S08) · ERP Agro Peruvian Foods',
      },
      // Módulo 4: Compras & Insumos (Elisban Huaylla)
      {
        path: 'compras',
        canActivate: [authGuard, rolGuard('SUPERVISOR', 'ADMIN')],
        loadComponent: () => import('./features/compras/compra-list').then((m) => m.CompraList),
        title: 'Compras de Insumos · ERP Agro Peruvian Foods',
      },
      {
        path: 'compras/proveedores',
        canActivate: [authGuard, rolGuard('SUPERVISOR', 'ADMIN')],
        loadComponent: () =>
          import('./features/compras/proveedor-list').then((m) => m.ProveedorList),
        title: 'Directorio de Proveedores · ERP Agro Peruvian Foods',
      },
      {
        path: 'compras/proveedores/nuevo',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () =>
          import('./features/compras/proveedor-form').then((m) => m.ProveedorForm),
        title: 'Registrar Proveedor · ERP Agro Peruvian Foods',
      },
      {
        path: 'sin-permiso',
        loadComponent: () => import('./core/sin-permiso/sin-permiso').then((m) => m.SinPermiso),
        title: 'Acceso Denegado (403) · ERP Agro Peruvian Foods',
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
