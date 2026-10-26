import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/services/api-service';
import { Tercero, TerceroCreateRequest, TipoTercero } from './terceros.model';

@Injectable({ providedIn: 'root' })
export class TercerosService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly basePath = '/api/v1/terceros';

  listar(tipo?: TipoTercero): Observable<Tercero[]> {
    let params = new HttpParams();
    if (tipo) {
      params = params.set('tipo', tipo);
    }
    return this.http.get<Tercero[]>(this.api.buildUrl(this.basePath), { params });
  }

  obtenerPorId(id: number): Observable<Tercero> {
    return this.http.get<Tercero>(this.api.buildUrl(`${this.basePath}/${id}`));
  }

  crear(req: TerceroCreateRequest): Observable<Tercero> {
    return this.http.post<Tercero>(this.api.buildUrl(this.basePath), req);
  }

  actualizar(id: number, req: TerceroCreateRequest): Observable<Tercero> {
    return this.http.put<Tercero>(this.api.buildUrl(`${this.basePath}/${id}`), req);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.basePath}/${id}`));
  }
}
