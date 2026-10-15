export type MetodoPagoVenta = 'EFECTIVO' | 'YAPE' | 'PLIN' | 'TRANSFERENCIA';

export interface DetalleVentaRequest {
  productoId: number;
  cantidad: number;
}

export interface DetalleVentaResponse {
  productoId: number;
  nombreProducto: string;
  precioUnitario: number;
  cantidad: number;
  subtotal: number;
}

export interface VentaRequest {
  metodoPago?: MetodoPagoVenta;
  clienteNombre?: string;
  detalles: DetalleVentaRequest[];
}

export interface VentaResponse {
  id: number;
  numeroTicket?: string;
  fecha: string;
  estado: string;
  total: number;
  detalles: DetalleVentaResponse[];
}
