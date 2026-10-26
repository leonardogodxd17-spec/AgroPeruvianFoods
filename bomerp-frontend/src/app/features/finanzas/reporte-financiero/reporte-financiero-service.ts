import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api-service';
import {
  DashboardFinanciero,
  FlujoCajaResumen,
  ReporteRendicionesAgregado,
} from './reporte-financiero.model';

@Injectable({ providedIn: 'root' })
export class ReporteFinancieroService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly basePath = '/api/v1/finanzas/reportes';

  obtenerDashboard(): Observable<DashboardFinanciero> {
    return this.http.get<DashboardFinanciero>(this.api.buildUrl(`${this.basePath}/dashboard`));
  }

  obtenerFlujoCaja(): Observable<FlujoCajaResumen> {
    return this.http.get<FlujoCajaResumen>(this.api.buildUrl(`${this.basePath}/flujo-caja`));
  }

  obtenerRendicionesAgregado(): Observable<ReporteRendicionesAgregado> {
    return this.http.get<ReporteRendicionesAgregado>(
      this.api.buildUrl(`${this.basePath}/rendiciones-agregado`)
    );
  }

  descargarCierreCaja(sesionId: number, formato: 'PDF' | 'EXCEL'): Observable<Blob> {
    const params = new HttpParams().set('formato', formato);
    return this.http.get(this.api.buildUrl(`${this.basePath}/cierre-caja/${sesionId}/exportar`), {
      params,
      responseType: 'blob',
    });
  }

  obtenerReporteOmnicanal(): Observable<import('./reporte-financiero.model').ReporteOmnicanal> {
    return this.http.get<import('./reporte-financiero.model').ReporteOmnicanal>(
      this.api.buildUrl('/api/v1/ventas/reporte-omnicanal')
    );
  }

  obtenerReporteSemaforo(): Observable<import('./reporte-financiero.model').ReporteSemaforoInventario> {
    return this.http.get<import('./reporte-financiero.model').ReporteSemaforoInventario>(
      this.api.buildUrl('/api/v1/inventario/reporte-semaforo')
    );
  }
}
