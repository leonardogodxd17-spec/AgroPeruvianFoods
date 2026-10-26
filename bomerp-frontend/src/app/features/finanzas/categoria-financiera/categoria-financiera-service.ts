import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import { CategoriaFinanciera, TipoFinanciero } from './categoria-financiera.model';

@Injectable({ providedIn: 'root' })
export class CategoriaFinancieraService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/finanzas/categorias';

  listar(tipo?: TipoFinanciero): Observable<CategoriaFinanciera[]> {
    let params = new HttpParams();
    if (tipo) {
      params = params.set('tipo', tipo);
    }
    return this.http.get<CategoriaFinanciera[]>(this.api.buildUrl(this.resource), { params });
  }

  obtener(id: number): Observable<CategoriaFinanciera> {
    return this.http.get<CategoriaFinanciera>(this.api.buildUrl(`${this.resource}/${id}`));
  }

  crear(categoria: CategoriaFinanciera): Observable<CategoriaFinanciera> {
    return this.http.post<CategoriaFinanciera>(this.api.buildUrl(this.resource), categoria);
  }

  actualizar(id: number, categoria: CategoriaFinanciera): Observable<CategoriaFinanciera> {
    return this.http.put<CategoriaFinanciera>(this.api.buildUrl(`${this.resource}/${id}`), categoria);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }
}
