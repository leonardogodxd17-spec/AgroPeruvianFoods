import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import {
  TransaccionFinanciera,
  TransaccionFinancieraRequest,
} from './transaccion-financiera.model';

@Injectable({ providedIn: 'root' })
export class TransaccionFinancieraService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/finanzas/transacciones';

  listar(categoriaFinancieraId?: number): Observable<TransaccionFinanciera[]> {
    let params = new HttpParams();
    if (categoriaFinancieraId) {
      params = params.set('categoriaFinancieraId', categoriaFinancieraId);
    }
    return this.http.get<TransaccionFinanciera[]>(this.api.buildUrl(this.resource), { params });
  }

  obtener(id: number): Observable<TransaccionFinanciera> {
    return this.http.get<TransaccionFinanciera>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  crear(request: TransaccionFinancieraRequest): Observable<TransaccionFinanciera> {
    return this.http.post<TransaccionFinanciera>(this.api.buildUrl(this.resource), request);
  }

  actualizar(id: number, request: TransaccionFinancieraRequest): Observable<TransaccionFinanciera> {
    return this.http.put<TransaccionFinanciera>(this.api.buildUrl(`${this.resource}/${id}`), request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }
}
