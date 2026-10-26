import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ConsultaDocumentoResponse {
  tipoDocumento: string;
  numeroDocumento: string;
  razonSocial: string;
  estado?: string;
  condicion?: string;
  direccion?: string;
  departamento?: string;
  provincia?: string;
  distrito?: string;
  exito: boolean;
  mensaje?: string;
}

export interface TipoCambioResponse {
  origen: string;
  moneda: string;
  fecha: string;
  compra: number;
  venta: number;
}

@Injectable({
  providedIn: 'root',
})
export class SunatApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/v1/sunat`;

  consultarDocumento(tipo: string, numero: string): Observable<ConsultaDocumentoResponse> {
    return this.http.get<ConsultaDocumentoResponse>(`${this.baseUrl}/consulta-documento`, {
      params: { tipo, numero },
    });
  }

  obtenerTipoCambio(): Observable<TipoCambioResponse> {
    return this.http.get<TipoCambioResponse>(`${this.baseUrl}/tipo-cambio`);
  }
}
