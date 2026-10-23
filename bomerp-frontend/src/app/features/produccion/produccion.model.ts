export interface DetalleConsumoInsumo {
  id?: number;
  insumoId: number;
  nombreInsumo: string;
  cantidadRequerida: number;
  cantidadConsumida?: number;
  unidadMedida: string;
}

export interface OrdenProduccion {
  id: number;
  codigoOrden: string;
  productoId: number;
  nombreProducto: string;
  cantidadProgramada: number;
  cantidadProducida?: number;
  fechaInicio?: string;
  fechaFin?: string;
  estado: 'PROGRAMADA' | 'EN_PROCESO' | 'COMPLETADA' | 'CANCELADA';
  observaciones?: string;
  insumos?: DetalleConsumoInsumo[];
}

export interface DetalleConsumoRequest {
  insumoId: number;
  nombreInsumo: string;
  cantidadRequerida: number;
  cantidadConsumida: number;
  unidadMedida: string;
}

export interface OrdenProduccionRequest {
  productoId: number;
  nombreProducto: string;
  cantidadProgramada: number;
  observaciones?: string;
  insumos: DetalleConsumoRequest[];
}

export interface CompletarOrdenRequest {
  cantidadProducida: number;
  fechaVencimiento: string;
}

export interface ProduccionReporte {
  totalOrdenes: number;
  ordenesCompletadas: number;
  unidadesTotalesProducidas: number;
}
