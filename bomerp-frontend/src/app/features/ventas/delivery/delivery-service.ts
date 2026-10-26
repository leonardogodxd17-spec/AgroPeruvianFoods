import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import {
  PedidoDelivery,
  CrearDeliveryRequest,
  ActualizarEstadoDeliveryRequest,
  AsignarRepartidorRequest,
  EstadoDelivery,
} from './delivery.model';

@Injectable({ providedIn: 'root' })
export class DeliveryService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly basePath = '/api/v1/ventas/delivery';

  listar(estado?: EstadoDelivery): Observable<PedidoDelivery[]> {
    let params = new HttpParams();
    if (estado) {
      params = params.set('estado', estado);
    }
    return this.http.get<PedidoDelivery[]>(this.api.buildUrl(this.basePath), { params });
  }

  obtenerPorId(id: number): Observable<PedidoDelivery> {
    return this.http.get<PedidoDelivery>(this.api.buildUrl(`${this.basePath}/${id}`));
  }

  crearPedido(req: CrearDeliveryRequest): Observable<PedidoDelivery> {
    return this.http.post<PedidoDelivery>(this.api.buildUrl(this.basePath), req);
  }

  actualizarEstado(id: number, req: ActualizarEstadoDeliveryRequest): Observable<PedidoDelivery> {
    return this.http.patch<PedidoDelivery>(this.api.buildUrl(`${this.basePath}/${id}/estado`), req);
  }

  asignarRepartidor(id: number, req: AsignarRepartidorRequest): Observable<PedidoDelivery> {
    return this.http.patch<PedidoDelivery>(this.api.buildUrl(`${this.basePath}/${id}/repartidor`), req);
  }
}
