import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  DescuentoFefoRequest,
  DescuentoFefoResponse,
  Lote,
  LoteCreateDto,
  MovimientoKardex,
} from './inventario.model';

@Injectable({
  providedIn: 'root',
})
export class InventarioService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/v1/inventario';

  listarLotes(productoId?: number): Observable<Lote[]> {
    let params = new HttpParams();
    if (productoId) {
      params = params.set('productoId', productoId);
    }
    return this.http.get<Lote[]>(`${this.apiUrl}/lotes`, { params });
  }

  listarAlertasVencimiento(dias = 30): Observable<Lote[]> {
    const params = new HttpParams().set('dias', dias);
    return this.http.get<Lote[]>(`${this.apiUrl}/lotes/alertas-vencimiento`, { params });
  }

  registrarLote(dto: LoteCreateDto): Observable<Lote> {
    return this.http.post<Lote>(`${this.apiUrl}/lotes`, dto);
  }

  despachoFefo(request: DescuentoFefoRequest): Observable<DescuentoFefoResponse> {
    return this.http.post<DescuentoFefoResponse>(`${this.apiUrl}/despacho-fefo`, request);
  }

  listarKardex(productoId?: number): Observable<MovimientoKardex[]> {
    let params = new HttpParams();
    if (productoId) {
      params = params.set('productoId', productoId);
    }
    return this.http.get<MovimientoKardex[]>(`${this.apiUrl}/kardex`, { params });
  }

  evaluarVencimientos(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/evaluar-vencimientos`, {});
  }
}
