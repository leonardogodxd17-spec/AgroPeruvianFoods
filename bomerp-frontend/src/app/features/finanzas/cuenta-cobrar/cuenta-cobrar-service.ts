import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import {
  CuentaPorCobrar,
  CuentaPorCobrarDetalle,
  CrearCreditoRequest,
  RegistrarAbonoRequest,
  EstadoCredito,
} from './cuenta-cobrar.model';

@Injectable({ providedIn: 'root' })
export class CuentaCobrarService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly basePath = '/api/v1/finanzas/creditos';

  listar(estado?: EstadoCredito): Observable<CuentaPorCobrar[]> {
    let params = new HttpParams();
    if (estado) {
      params = params.set('estado', estado);
    }
    return this.http.get<CuentaPorCobrar[]>(this.api.buildUrl(this.basePath), { params });
  }

  obtenerPorId(id: number): Observable<CuentaPorCobrarDetalle> {
    return this.http.get<CuentaPorCobrarDetalle>(this.api.buildUrl(`${this.basePath}/${id}`));
  }

  registrarCredito(request: CrearCreditoRequest): Observable<CuentaPorCobrar> {
    return this.http.post<CuentaPorCobrar>(this.api.buildUrl(this.basePath), request);
  }

  registrarAbono(id: number, request: RegistrarAbonoRequest): Observable<CuentaPorCobrarDetalle> {
    return this.http.post<CuentaPorCobrarDetalle>(
      this.api.buildUrl(`${this.basePath}/${id}/abonos`),
      request
    );
  }
}
