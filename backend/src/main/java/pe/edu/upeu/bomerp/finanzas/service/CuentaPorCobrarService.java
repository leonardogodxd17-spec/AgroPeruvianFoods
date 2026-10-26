package pe.edu.upeu.bomerp.finanzas.service;

import pe.edu.upeu.bomerp.finanzas.dto.CrearCreditoRequest;
import pe.edu.upeu.bomerp.finanzas.dto.CuentaPorCobrarDetalleResponse;
import pe.edu.upeu.bomerp.finanzas.dto.CuentaPorCobrarResponse;
import pe.edu.upeu.bomerp.finanzas.dto.RegistrarAbonoRequest;
import pe.edu.upeu.bomerp.finanzas.entity.EstadoCredito;

import java.util.List;

public interface CuentaPorCobrarService {
    List<CuentaPorCobrarResponse> listarTodos(EstadoCredito estado);
    CuentaPorCobrarDetalleResponse obtenerPorId(Long id);
    CuentaPorCobrarResponse registrarCredito(CrearCreditoRequest req);
    CuentaPorCobrarDetalleResponse registrarAbono(Long creditoId, RegistrarAbonoRequest req);
}
