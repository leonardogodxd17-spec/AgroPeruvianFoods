import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import {
  OrdenProduccion,
  OrdenProduccionRequest,
  CompletarOrdenRequest,
  ProduccionReporte,
} from './produccion.model';

@Injectable({ providedIn: 'root' })
export class ProduccionService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  listarOrdenes(): Observable<OrdenProduccion[]> {
    return this.http.get<OrdenProduccion[]>(this.api.buildUrl('/api/v1/produccion/ordenes'));
  }

  obtenerOrden(id: number): Observable<OrdenProduccion> {
    return this.http.get<OrdenProduccion>(this.api.buildUrl(`/api/v1/produccion/ordenes/${id}`));
  }

  crearOrden(request: OrdenProduccionRequest): Observable<OrdenProduccion> {
    return this.http.post<OrdenProduccion>(this.api.buildUrl('/api/v1/produccion/ordenes'), request);
  }

  iniciarOrden(id: number): Observable<OrdenProduccion> {
    return this.http.put<OrdenProduccion>(this.api.buildUrl(`/api/v1/produccion/ordenes/${id}/iniciar`), {});
  }

  completarOrden(id: number, request: CompletarOrdenRequest): Observable<OrdenProduccion> {
    return this.http.put<OrdenProduccion>(
      this.api.buildUrl(`/api/v1/produccion/ordenes/${id}/completar`),
      request
    );
  }

  cancelarOrden(id: number): Observable<OrdenProduccion> {
    return this.http.post<OrdenProduccion>(this.api.buildUrl(`/api/v1/produccion/ordenes/${id}/cancelar`), {});
  }

  reporte(): Observable<ProduccionReporte> {
    return this.http.get<ProduccionReporte>(this.api.buildUrl('/api/v1/produccion/resumen'));
  }
}
