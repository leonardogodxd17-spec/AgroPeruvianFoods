package pe.edu.upeu.bomerp.finanzas.service;

import pe.edu.upeu.bomerp.finanzas.dto.AperturaSesionRequest;
import pe.edu.upeu.bomerp.finanzas.dto.CierreCajaReporteDto;
import pe.edu.upeu.bomerp.finanzas.dto.CierreSesionRequest;
import pe.edu.upeu.bomerp.finanzas.dto.MovimientoCajaRequest;
import pe.edu.upeu.bomerp.finanzas.dto.MovimientoCajaResponse;
import pe.edu.upeu.bomerp.finanzas.dto.SesionCajaDetalleResponse;
import pe.edu.upeu.bomerp.finanzas.dto.SesionCajaResponse;

import java.util.List;

public interface SesionCajaService {

    SesionCajaResponse abrirSesion(AperturaSesionRequest request);

    SesionCajaResponse cerrarSesion(Long id, CierreSesionRequest request);

    MovimientoCajaResponse registrarMovimiento(Long sesionId, MovimientoCajaRequest request);

    List<SesionCajaResponse> listarSesiones();

    SesionCajaDetalleResponse obtenerDetalle(Long id);

    SesionCajaResponse obtenerSesionActivaUsuario(Long usuarioId);

    java.util.Optional<SesionCajaResponse> buscarUltimaSesionActiva();

    SesionCajaResponse obtenerUltimaSesionActiva();

    CierreCajaReporteDto generarDatosReporte(Long id);

    List<pe.edu.upeu.bomerp.finanzas.dto.DenominacionMonedaResponse> listarDenominaciones();
}
