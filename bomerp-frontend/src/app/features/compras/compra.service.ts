import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import {
  Proveedor,
  ProveedorRequest,
  Compra,
  AmortizarPagoRequest,
  CompraReporte,
} from './compra.model';

@Injectable({ providedIn: 'root' })
export class CompraService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  listarProveedores(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(this.api.buildUrl('/api/v1/compras/proveedores'));
  }

  crearProveedor(request: ProveedorRequest): Observable<Proveedor> {
    return this.http.post<Proveedor>(this.api.buildUrl('/api/v1/compras/proveedores'), request);
  }

  listarCompras(): Observable<Compra[]> {
    return this.http.get<Compra[]>(this.api.buildUrl('/api/v1/compras'));
  }

  obtenerCompra(id: number): Observable<Compra> {
    return this.http.get<Compra>(this.api.buildUrl(`/api/v1/compras/${id}`));
  }

  amortizarPago(id: number, request: AmortizarPagoRequest): Observable<Compra> {
    return this.http.put<Compra>(this.api.buildUrl(`/api/v1/compras/${id}/amortizar`), request);
  }

  anularCompra(id: number): Observable<Compra> {
    return this.http.post<Compra>(this.api.buildUrl(`/api/v1/compras/${id}/anular`), {});
  }

  reporte(): Observable<CompraReporte> {
    return this.http.get<CompraReporte>(this.api.buildUrl('/api/v1/compras/resumen'));
  }
}
