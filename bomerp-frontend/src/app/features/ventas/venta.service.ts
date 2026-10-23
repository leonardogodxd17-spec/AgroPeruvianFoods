import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { VentaRequest, VentaResponse, VentaReporte } from './venta.model';

@Injectable({ providedIn: 'root' })
export class VentaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/ventas';

  crear(venta: VentaRequest): Observable<VentaResponse> {
    return this.http.post<VentaResponse>(this.api.buildUrl(this.resource), venta);
  }

  buscar(
    estado?: string,
    desde?: string,
    hasta?: string,
    ordenarPor = 'fecha',
    direccion = 'DESC',
  ): Observable<VentaResponse[]> {
    let params = new HttpParams().set('ordenarPor', ordenarPor).set('direccion', direccion);
    if (estado) params = params.set('estado', estado);
    if (desde) params = params.set('desde', desde);
    if (hasta) params = params.set('hasta', hasta);
    return this.http.get<VentaResponse[]>(this.api.buildUrl(this.resource), { params });
  }

  obtener(id: number): Observable<VentaResponse> {
    return this.http.get<VentaResponse>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  anular(id: number): Observable<VentaResponse> {
    return this.http.patch<VentaResponse>(this.api.buildUrl(`${this.resource}/${id}/anular`), {});
  }

  reporte(estado?: string, desde?: string, hasta?: string): Observable<VentaReporte> {
    let params = new HttpParams();
    if (estado) params = params.set('estado', estado);
    if (desde) params = params.set('desde', desde);
    if (hasta) params = params.set('hasta', hasta);
    return this.http.get<VentaReporte>(this.api.buildUrl(`${this.resource}/resumen`), { params });
  }

  eliminarTodas(): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(this.resource));
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }
}
