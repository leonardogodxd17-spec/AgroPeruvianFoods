import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import {
  AperturaSesionRequest,
  CierreCajaReporte,
  CierreSesionRequest,
  MovimientoCaja,
  MovimientoCajaRequest,
  SesionCaja,
  SesionCajaDetalle,
} from './sesion-caja.model';

@Injectable({ providedIn: 'root' })
export class SesionCajaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly basePath = '/api/v1/finanzas/sesiones';
  private readonly reportesPath = '/api/v1/finanzas/reportes';

  listar(): Observable<SesionCaja[]> {
    return this.http.get<SesionCaja[]>(this.api.buildUrl(this.basePath));
  }

  listarDenominaciones(): Observable<import('./sesion-caja.model').DenominacionMoneda[]> {
    return this.http.get<import('./sesion-caja.model').DenominacionMoneda[]>(
      this.api.buildUrl('/api/v1/finanzas/denominaciones')
    );
  }

  obtenerDetalle(id: number): Observable<SesionCajaDetalle> {
    return this.http.get<SesionCajaDetalle>(this.api.buildUrl(`${this.basePath}/${id}`));
  }

  obtenerActiva(): Observable<SesionCaja> {
    return this.http.get<SesionCaja>(this.api.buildUrl(`${this.basePath}/activa`));
  }

  abrir(request: AperturaSesionRequest): Observable<SesionCaja> {
    return this.http.post<SesionCaja>(this.api.buildUrl(`${this.basePath}/abrir`), request);
  }

  cerrar(id: number, request: CierreSesionRequest): Observable<SesionCaja> {
    return this.http.post<SesionCaja>(this.api.buildUrl(`${this.basePath}/${id}/cerrar`), request);
  }

  registrarMovimiento(id: number, request: MovimientoCajaRequest): Observable<MovimientoCaja> {
    return this.http.post<MovimientoCaja>(
      this.api.buildUrl(`${this.basePath}/${id}/movimientos`),
      request
    );
  }

  obtenerDatosCierre(id: number): Observable<CierreCajaReporte> {
    return this.http.get<CierreCajaReporte>(this.api.buildUrl(`${this.basePath}/${id}/datos-cierre`));
  }

  exportar(id: number, formato: 'PDF' | 'EXCEL'): Observable<Blob> {
    const params = new HttpParams().set('formato', formato);
    return this.http.get(this.api.buildUrl(`${this.reportesPath}/cierre-caja/${id}/exportar`), {
      params,
      responseType: 'blob',
    });
  }
}
