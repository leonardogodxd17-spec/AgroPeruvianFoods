import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import {
  SesionCaja,
  MovimientoCaja,
  MovimientoCajaRequest,
  AperturaCajaRequest,
  CierreCajaRequest,
  CajaReporte,
} from './caja.model';

@Injectable({ providedIn: 'root' })
export class CajaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  obtenerSesionActiva(): Observable<SesionCaja> {
    return this.http.get<SesionCaja>(this.api.buildUrl('/api/v1/caja/activa'));
  }

  listarHistorial(): Observable<SesionCaja[]> {
    return this.http.get<SesionCaja[]>(this.api.buildUrl('/api/v1/caja/historial'));
  }

  registrarMovimiento(request: MovimientoCajaRequest): Observable<MovimientoCaja> {
    return this.http.post<MovimientoCaja>(this.api.buildUrl('/api/v1/caja/movimientos'), request);
  }

  abrirCaja(request: AperturaCajaRequest): Observable<SesionCaja> {
    return this.http.post<SesionCaja>(this.api.buildUrl('/api/v1/caja/apertura'), request);
  }

  cerrarCaja(id: number, request: CierreCajaRequest): Observable<SesionCaja> {
    return this.http.post<SesionCaja>(this.api.buildUrl(`/api/v1/caja/${id}/cierre`), request);
  }

  obtenerResumen(): Observable<CajaReporte> {
    return this.http.get<CajaReporte>(this.api.buildUrl('/api/v1/caja/resumen'));
  }
}
