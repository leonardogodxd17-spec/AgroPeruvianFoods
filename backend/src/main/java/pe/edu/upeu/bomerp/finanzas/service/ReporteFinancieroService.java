package pe.edu.upeu.bomerp.finanzas.service;

import pe.edu.upeu.bomerp.finanzas.dto.DashboardFinancieroResponse;
import pe.edu.upeu.bomerp.finanzas.dto.FlujoCajaResumenResponse;
import pe.edu.upeu.bomerp.finanzas.dto.ReporteRendicionesAgregadoDto;
import pe.edu.upeu.bomerp.finanzas.exporter.FormatoReporte;

public interface ReporteFinancieroService {

    DashboardFinancieroResponse obtenerDashboard();

    FlujoCajaResumenResponse obtenerFlujoCaja();

    ReporteRendicionesAgregadoDto obtenerReporteRendiciones();

    byte[] exportarCierreCaja(Long sesionId, FormatoReporte formato);
}
