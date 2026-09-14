package pe.edu.upeu.bomerp.caja.service;

import pe.edu.upeu.bomerp.caja.dto.*;

import java.util.List;

public interface CajaService {
    SesionCajaResponse abrirCaja(AperturaCajaRequest request);
    MovimientoCajaResponse registrarMovimiento(MovimientoCajaRequest request);
    SesionCajaResponse cerrarCaja(Long sesionId, CierreCajaRequest request);
    SesionCajaResponse obtenerSesionActiva();
    List<SesionCajaResponse> listarHistorial();
}
