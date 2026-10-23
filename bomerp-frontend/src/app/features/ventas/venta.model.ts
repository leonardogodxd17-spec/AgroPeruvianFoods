export interface DetalleVentaRequest {
  productoId: number;
  cantidad: number;
}

export interface DetalleVentaResponse {
  id?: number;
  productoId: number;
  nombreProducto: string;
  precioUnitario: number;
  cantidad: number;
  subtotal: number;
}

export type DetalleVenta = DetalleVentaResponse;

export interface VentaRequest {
  clienteNombre?: string;
  metodoPago?: 'EFECTIVO' | 'YAPE' | 'PLIN' | 'TRANSFERENCIA';
  detalles: DetalleVentaRequest[];
}

export interface VentaResponse {
  id: number;
  numeroTicket?: string;
  fecha: string;
  estado: string;
  total: number;
  subtotal?: number;
  igv?: number;
  clienteNombre?: string;
  metodoPago?: string;
  detalles: DetalleVentaResponse[];
}

export type Venta = VentaResponse;

export interface VentaResumen {
  id: number;
  fecha: string;
  estado: string;
  total: number;
  cantidadDetalles: number;
}

export interface VentaAgregado {
  totalVentas: number;
  montoTotal: number;
  ticketPromedio: number;
}

export interface VentaReporte {
  totalVentas?: number;
  totalRecaudado?: number;
  montoTotal?: number;
  ticketPromedio?: number;
  agregado?: VentaAgregado;
  ventas?: VentaResumen[];
}
