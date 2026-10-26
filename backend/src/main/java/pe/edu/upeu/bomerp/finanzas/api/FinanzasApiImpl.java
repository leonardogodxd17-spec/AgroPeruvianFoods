package pe.edu.upeu.bomerp.finanzas.api;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.finanzas.dto.MovimientoCajaRequest;
import pe.edu.upeu.bomerp.finanzas.dto.MovimientoCajaResponse;
import pe.edu.upeu.bomerp.finanzas.entity.CategoriaFinanciera;
import pe.edu.upeu.bomerp.finanzas.entity.EstadoCaja;
import pe.edu.upeu.bomerp.finanzas.entity.MetodoPago;
import pe.edu.upeu.bomerp.finanzas.entity.SesionCaja;
import pe.edu.upeu.bomerp.finanzas.entity.TipoFinanciero;
import pe.edu.upeu.bomerp.finanzas.repository.CategoriaFinancieraRepository;
import pe.edu.upeu.bomerp.finanzas.repository.SesionCajaRepository;
import pe.edu.upeu.bomerp.finanzas.service.SesionCajaService;

import java.math.BigDecimal;

@Service
public class FinanzasApiImpl implements FinanzasApi {

    private final SesionCajaRepository sesionRepository;
    private final SesionCajaService sesionService;
    private final CategoriaFinancieraRepository categoriaRepository;

    public FinanzasApiImpl(
            SesionCajaRepository sesionRepository,
            SesionCajaService sesionService,
            CategoriaFinancieraRepository categoriaRepository) {
        this.sesionRepository = sesionRepository;
        this.sesionService = sesionService;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public Long registrarCobroVenta(Long usuarioId, BigDecimal monto, MetodoPago metodoPago, String referenciaVenta, Long categoriaId) {
        SesionCaja sesion = sesionRepository.findFirstByUsuarioIdAndEstadoOrderByFechaAperturaDesc(usuarioId, EstadoCaja.ABIERTA)
                .orElseThrow(() -> new IllegalStateException(
                        "No se puede registrar el cobro de la venta " + referenciaVenta +
                        " porque el usuario " + usuarioId + " no tiene un turno de caja ABIERTO."
                ));

        Long catId = categoriaId;
        if (catId == null) {
            catId = categoriaRepository.findAll().stream()
                    .filter(c -> c.getTipoDefault() == TipoFinanciero.INGRESO)
                    .map(CategoriaFinanciera::getId)
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró categoría de ingreso para registrar el cobro."));
        }

        MovimientoCajaRequest movReq = MovimientoCajaRequest.builder()
                .tipo(TipoFinanciero.INGRESO)
                .metodoPago(metodoPago)
                .monto(monto)
                .descripcion("Cobro de Venta " + (referenciaVenta != null ? referenciaVenta : ""))
                .referenciaExterna(referenciaVenta)
                .categoriaFinancieraId(catId)
                .build();

        MovimientoCajaResponse resp = sesionService.registrarMovimiento(sesion.getId(), movReq);
        return resp.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneSesionAbierta(Long usuarioId) {
        return sesionRepository.existsByUsuarioIdAndEstado(usuarioId, EstadoCaja.ABIERTA);
    }

    @Override
    @Transactional(readOnly = true)
    public Long obtenerIdSesionActiva(Long usuarioId) {
        return sesionRepository.findFirstByUsuarioIdAndEstadoOrderByFechaAperturaDesc(usuarioId, EstadoCaja.ABIERTA)
                .map(SesionCaja::getId)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no tiene una sesión de caja activa."));
    }
}
