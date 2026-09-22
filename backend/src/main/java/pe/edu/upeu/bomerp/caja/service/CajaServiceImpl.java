package pe.edu.upeu.bomerp.caja.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.caja.dto.*;
import pe.edu.upeu.bomerp.caja.entity.EstadoSesionCaja;
import pe.edu.upeu.bomerp.caja.entity.MovimientoCaja;
import pe.edu.upeu.bomerp.caja.entity.SesionCaja;
import pe.edu.upeu.bomerp.caja.entity.TipoMovimientoCaja;
import pe.edu.upeu.bomerp.caja.repository.MovimientoCajaRepository;
import pe.edu.upeu.bomerp.caja.repository.SesionCajaRepository;
import pe.edu.upeu.bomerp.exception.BusinessConflictException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajaServiceImpl implements CajaService {

    private final SesionCajaRepository sesionCajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;

    @Override
    @Transactional
    public SesionCajaResponse abrirCaja(AperturaCajaRequest request) {
        sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA)
                .ifPresent(s -> {
                    throw new BusinessConflictException("Ya existe una sesión de caja abierta (ID: " + s.getId()
                            + ", Cajero: " + s.getCajeroNombre() + "). Debe cerrarla antes de abrir una nueva.");
                });

        SesionCaja sesion = SesionCaja.builder()
                .cajeroNombre(request.getCajeroNombre())
                .fechaApertura(LocalDateTime.now())
                .montoApertura(request.getMontoApertura())
                .saldoTeorico(request.getMontoApertura())
                .estado(EstadoSesionCaja.ABIERTA)
                .movimientos(new ArrayList<>())
                .build();

        return toSesionResponse(sesionCajaRepository.save(sesion));
    }

    @Override
    @Transactional
    public MovimientoCajaResponse registrarMovimiento(MovimientoCajaRequest request) {
        SesionCaja sesion = sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA)
                .orElseThrow(() -> new BusinessConflictException("No existe ninguna sesión de caja abierta para registrar movimientos."));

        if (request.getTipo() == TipoMovimientoCaja.EGRESO && sesion.getSaldoTeorico().compareTo(request.getMonto()) < 0) {
            throw new BusinessConflictException("Saldo insuficiente en caja chica para realizar el egreso. Saldo disponible: "
                    + sesion.getSaldoTeorico() + ", solicitado: " + request.getMonto());
        }

        MovimientoCaja mov = MovimientoCaja.builder()
                .sesionCaja(sesion)
                .tipo(request.getTipo())
                .monto(request.getMonto())
                .concepto(request.getConcepto())
                .metodoPago(request.getMetodoPago())
                .referencia(request.getReferencia())
                .fecha(LocalDateTime.now())
                .build();

        if (request.getTipo() == TipoMovimientoCaja.INGRESO) {
            sesion.setSaldoTeorico(sesion.getSaldoTeorico().add(request.getMonto()));
        } else {
            sesion.setSaldoTeorico(sesion.getSaldoTeorico().subtract(request.getMonto()));
        }

        sesion.getMovimientos().add(mov);
        sesionCajaRepository.save(sesion);

        return toMovimientoResponse(mov);
    }

    @Override
    @Transactional
    public SesionCajaResponse cerrarCaja(Long sesionId, CierreCajaRequest request) {
        SesionCaja sesion = sesionCajaRepository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de caja no encontrada con ID: " + sesionId));

        if (sesion.getEstado() == EstadoSesionCaja.CERRADA) {
            throw new BusinessConflictException("La sesión de caja ya se encuentra cerrada.");
        }

        BigDecimal diferencia = request.getSaldoReal().subtract(sesion.getSaldoTeorico());

        sesion.setFechaCierre(LocalDateTime.now());
        sesion.setMontoCierre(request.getSaldoReal());
        sesion.setSaldoReal(request.getSaldoReal());
        sesion.setDiferencia(diferencia);
        sesion.setEstado(EstadoSesionCaja.CERRADA);
        sesion.setObservaciones(request.getObservaciones());

        return toSesionResponse(sesionCajaRepository.save(sesion));
    }

    @Override
    @Transactional(readOnly = true)
    public SesionCajaResponse obtenerSesionActiva() {
        SesionCaja sesion = sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA)
                .orElseThrow(() -> new ResourceNotFoundException("No hay ninguna sesión de caja actualmente abierta."));
        return toSesionResponse(sesion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SesionCajaResponse> listarHistorial() {
        return sesionCajaRepository.findAll().stream()
                .map(this::toSesionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CajaReporte reporteResumen() {
        List<SesionCaja> sesiones = sesionCajaRepository.findAll();

        long totalSesiones = sesiones.size();
        long abiertas = sesiones.stream().filter(s -> s.getEstado() == EstadoSesionCaja.ABIERTA).count();
        long cerradas = sesiones.stream().filter(s -> s.getEstado() == EstadoSesionCaja.CERRADA).count();

        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalEgresos = BigDecimal.ZERO;
        BigDecimal saldoNeto = BigDecimal.ZERO;
        BigDecimal diferenciasAcumuladas = BigDecimal.ZERO;

        for (SesionCaja s : sesiones) {
            if (s.getSaldoTeorico() != null) {
                saldoNeto = saldoNeto.add(s.getSaldoTeorico());
            }
            if (s.getDiferencia() != null) {
                diferenciasAcumuladas = diferenciasAcumuladas.add(s.getDiferencia());
            }
            if (s.getMovimientos() != null) {
                for (MovimientoCaja m : s.getMovimientos()) {
                    if (m.getTipo() == TipoMovimientoCaja.INGRESO) {
                        totalIngresos = totalIngresos.add(m.getMonto());
                    } else if (m.getTipo() == TipoMovimientoCaja.EGRESO) {
                        totalEgresos = totalEgresos.add(m.getMonto());
                    }
                }
            }
        }

        List<SesionCajaResponse> listaResponses = sesiones.stream().map(this::toSesionResponse).toList();

        return CajaReporte.builder()
                .totalSesiones(totalSesiones)
                .sesionesAbiertas(abiertas)
                .sesionesCerradas(cerradas)
                .totalIngresos(totalIngresos)
                .totalEgresos(totalEgresos)
                .saldoNetoEfectivo(saldoNeto)
                .diferenciasAcumuladas(diferenciasAcumuladas)
                .sesiones(listaResponses)
                .build();
    }

    private SesionCajaResponse toSesionResponse(SesionCaja s) {
        List<MovimientoCajaResponse> movs = s.getMovimientos() != null
                ? s.getMovimientos().stream().map(this::toMovimientoResponse).toList()
                : List.of();

        return SesionCajaResponse.builder()
                .id(s.getId())
                .cajeroNombre(s.getCajeroNombre())
                .fechaApertura(s.getFechaApertura())
                .fechaCierre(s.getFechaCierre())
                .montoApertura(s.getMontoApertura())
                .montoCierre(s.getMontoCierre())
                .saldoTeorico(s.getSaldoTeorico())
                .saldoReal(s.getSaldoReal())
                .diferencia(s.getDiferencia())
                .estado(s.getEstado())
                .observaciones(s.getObservaciones())
                .movimientos(movs)
                .build();
    }

    private MovimientoCajaResponse toMovimientoResponse(MovimientoCaja m) {
        return MovimientoCajaResponse.builder()
                .id(m.getId())
                .tipo(m.getTipo())
                .monto(m.getMonto())
                .concepto(m.getConcepto())
                .metodoPago(m.getMetodoPago())
                .referencia(m.getReferencia())
                .fecha(m.getFecha())
                .build();
    }
}
