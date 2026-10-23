export interface Proveedor {
  id: number;
  ruc: string;
  razonSocial: string;
  telefono?: string;
  email?: string;
  direccion?: string;
}

export interface ProveedorRequest {
  ruc: string;
  razonSocial: string;
  telefono?: string;
  email?: string;
  direccion?: string;
}

export interface DetalleCompra {
  id?: number;
  compraId?: number;
  insumoId: number;
  nombreInsumo?: string;
  cantidad: number;
  precioUnitario: number;
  subtotal?: number;
}

export interface Compra {
  id: number;
  numeroComprobante: string;
  proveedorId: number;
  fechaEmision: string;
  estado: 'REGISTRADA' | 'PAGADA' | 'ANULADA';
  subtotal: number;
  igv: number;
  total: number;
  saldoPendiente: number;
  detalles?: DetalleCompra[];
}

export interface AmortizarPagoRequest {
  montoAmortizado: number;
  metodoPago: string;
  referencia?: string;
}

export interface CompraReporte {
  totalCompras: number;
  totalFacturado: number;
  totalPendientePago: number;
}
