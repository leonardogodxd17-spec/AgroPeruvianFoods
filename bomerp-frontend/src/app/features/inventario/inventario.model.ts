export type EstadoLote = 'DISPONIBLE' | 'PROXIMO_A_VENCER' | 'VENCIDO' | 'AGOTADO';
export type TipoMovimientoKardex = 'ENTRADA' | 'SALIDA' | 'AJUSTE';

export interface Lote {
  id: number;
  codigoLote: string;
  productoId: number;
  productoNombre: string;
  fechaFabricacion: string;
  fechaVencimiento: string;
  stockInicial: number;
  stockActual: number;
  costoUnitario: number;
  estado: EstadoLote;
  observaciones?: string;
  diasParaVencer: number;
}

export interface LoteCreateDto {
  codigoLote: string;
  productoId: number;
  fechaFabricacion: string;
  fechaVencimiento: string;
  stockInicial: number;
  costoUnitario: number;
  observaciones?: string;
}

export interface LoteDescontadoItem {
  loteId: number;
  codigoLote: string;
  fechaVencimiento: string;
  cantidadDescontada: number;
  stockRestanteLote: number;
  loteAgotado: boolean;
}

export interface DescuentoFefoRequest {
  productoId: number;
  cantidad: number;
  motivo: string;
  documentoReferencia?: string;
}

export interface DescuentoFefoResponse {
  productoId: number;
  productoNombre: string;
  cantidadTotalSolicitada: number;
  cantidadTotalDescontada: number;
  stockTotalRestante: number;
  lotesAfectados: LoteDescontadoItem[];
}

export interface MovimientoKardex {
  id: number;
  tipo: TipoMovimientoKardex;
  loteId?: number;
  codigoLote?: string;
  productoId: number;
  productoNombre: string;
  cantidad: number;
  saldoStockResultante: number;
  motivo: string;
  documentoReferencia?: string;
  fechaHora: string;
}
