package pe.edu.upeu.bomerp.caja;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.bomerp.caja.dto.*;
import pe.edu.upeu.bomerp.caja.entity.EstadoSesionCaja;
import pe.edu.upeu.bomerp.caja.entity.MetodoPagoCaja;
import pe.edu.upeu.bomerp.caja.entity.MovimientoCaja;
import pe.edu.upeu.bomerp.caja.entity.SesionCaja;
import pe.edu.upeu.bomerp.caja.entity.TipoMovimientoCaja;
import pe.edu.upeu.bomerp.caja.repository.MovimientoCajaRepository;
import pe.edu.upeu.bomerp.caja.repository.SesionCajaRepository;
import pe.edu.upeu.bomerp.caja.service.CajaServiceImpl;
import pe.edu.upeu.bomerp.exception.BusinessConflictException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CajaServiceTest {

    @Mock
    private SesionCajaRepository sesionCajaRepository;

    @Mock
    private MovimientoCajaRepository movimientoCajaRepository;

    @InjectMocks
    private CajaServiceImpl cajaService;

    private SesionCaja sesionAbierta;

    @BeforeEach
    void setUp() {
        sesionAbierta = SesionCaja.builder()
                .id(1L)
                .cajeroNombre("Brandon Ccalla")
                .fechaApertura(LocalDateTime.now())
                .montoApertura(new BigDecimal("500.00"))
                .saldoTeorico(new BigDecimal("500.00"))
                .estado(EstadoSesionCaja.ABIERTA)
                .movimientos(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Debe abrir caja exitosamente si no hay sesiones previas abiertas")
    void abrirCaja_sinSesionPrevia_debeCrearSesion() {
        AperturaCajaRequest req = new AperturaCajaRequest();
        req.setCajeroNombre("Brandon Ccalla");
        req.setMontoApertura(new BigDecimal("300.00"));

        when(sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA))
                .thenReturn(Optional.empty());

        when(sesionCajaRepository.save(any(SesionCaja.class))).thenAnswer(i -> {
            SesionCaja s = i.getArgument(0);
            s.setId(2L);
            return s;
        });

        SesionCajaResponse resp = cajaService.abrirCaja(req);

        assertNotNull(resp);
        assertEquals("Brandon Ccalla", resp.getCajeroNombre());
        assertEquals(new BigDecimal("300.00"), resp.getMontoApertura());
        assertEquals(EstadoSesionCaja.ABIERTA, resp.getEstado());
        verify(sesionCajaRepository, times(1)).save(any(SesionCaja.class));
    }

    @Test
    @DisplayName("Debe rechazar apertura de caja si ya existe una sesión abierta")
    void abrirCaja_conSesionAbierta_debeLanzarConflicto() {
        AperturaCajaRequest req = new AperturaCajaRequest();
        req.setCajeroNombre("Otro Cajero");
        req.setMontoApertura(new BigDecimal("200.00"));

        when(sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA))
                .thenReturn(Optional.of(sesionAbierta));

        assertThrows(BusinessConflictException.class, () -> cajaService.abrirCaja(req));
        verify(sesionCajaRepository, never()).save(any(SesionCaja.class));
    }

    @Test
    @DisplayName("Debe registrar ingreso de efectivo y aumentar el saldo teórico de la caja")
    void registrarMovimiento_ingreso_debeAumentarSaldoTeorico() {
        MovimientoCajaRequest req = new MovimientoCajaRequest();
        req.setTipo(TipoMovimientoCaja.INGRESO);
        req.setMonto(new BigDecimal("150.00"));
        req.setConcepto("Cobro venta");
        req.setMetodoPago(MetodoPagoCaja.EFECTIVO);

        when(sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA))
                .thenReturn(Optional.of(sesionAbierta));

        MovimientoCajaResponse resp = cajaService.registrarMovimiento(req);

        assertNotNull(resp);
        assertEquals(new BigDecimal("150.00"), resp.getMonto());
        assertEquals(new BigDecimal("650.00"), sesionAbierta.getSaldoTeorico());
        verify(sesionCajaRepository, times(1)).save(sesionAbierta);
    }

    @Test
    @DisplayName("Debe rechazar egreso si el saldo disponible en caja es insuficiente")
    void registrarMovimiento_egresoSinSaldo_debeLanzarConflicto() {
        MovimientoCajaRequest req = new MovimientoCajaRequest();
        req.setTipo(TipoMovimientoCaja.EGRESO);
        req.setMonto(new BigDecimal("800.00"));
        req.setConcepto("Pago urgente");
        req.setMetodoPago(MetodoPagoCaja.EFECTIVO);

        when(sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA))
                .thenReturn(Optional.of(sesionAbierta));

        assertThrows(BusinessConflictException.class, () -> cajaService.registrarMovimiento(req));
        assertEquals(new BigDecimal("500.00"), sesionAbierta.getSaldoTeorico());
    }

    @Test
    @DisplayName("Debe cerrar caja calculando la diferencia de arqueo (faltante/sobrante)")
    void cerrarCaja_conDiferencia_debeCalcularArqueo() {
        CierreCajaRequest req = new CierreCajaRequest();
        req.setSaldoReal(new BigDecimal("490.00"));
        req.setObservaciones("Arqueo de turno con faltante menor");

        when(sesionCajaRepository.findById(1L)).thenReturn(Optional.of(sesionAbierta));
        when(sesionCajaRepository.save(any(SesionCaja.class))).thenAnswer(i -> i.getArgument(0));

        SesionCajaResponse resp = cajaService.cerrarCaja(1L, req);

        assertNotNull(resp);
        assertEquals(EstadoSesionCaja.CERRADA, resp.getEstado());
        assertEquals(new BigDecimal("490.00"), resp.getSaldoReal());
        assertEquals(new BigDecimal("-10.00"), resp.getDiferencia());
        verify(sesionCajaRepository, times(1)).save(sesionAbierta);
    }

    @Test
    @DisplayName("Debe consolidar ingresos, egresos y saldos en el reporte resumen de caja")
    void reporteResumen_debeConsolidarIngresosEgresosYSaldos() {
        MovimientoCaja m1 = MovimientoCaja.builder()
                .id(1L)
                .tipo(TipoMovimientoCaja.INGRESO)
                .monto(new BigDecimal("100.00"))
                .concepto("Cobro venta")
                .metodoPago(MetodoPagoCaja.EFECTIVO)
                .build();
        MovimientoCaja m2 = MovimientoCaja.builder()
                .id(2L)
                .tipo(TipoMovimientoCaja.EGRESO)
                .monto(new BigDecimal("30.00"))
                .concepto("Compra insumo menor")
                .metodoPago(MetodoPagoCaja.EFECTIVO)
                .build();

        sesionAbierta.getMovimientos().add(m1);
        sesionAbierta.getMovimientos().add(m2);
        sesionAbierta.setSaldoTeorico(new BigDecimal("570.00"));
        sesionAbierta.setDiferencia(new BigDecimal("0.00"));

        when(sesionCajaRepository.findAll()).thenReturn(List.of(sesionAbierta));

        CajaReporte rep = cajaService.reporteResumen();

        assertNotNull(rep);
        assertEquals(1L, rep.getTotalSesiones());
        assertEquals(1L, rep.getSesionesAbiertas());
        assertEquals(0L, rep.getSesionesCerradas());
        assertEquals(new BigDecimal("100.00"), rep.getTotalIngresos());
        assertEquals(new BigDecimal("30.00"), rep.getTotalEgresos());
        assertEquals(new BigDecimal("570.00"), rep.getSaldoNetoEfectivo());
        assertEquals(new BigDecimal("0.00"), rep.getDiferenciasAcumuladas());
    }
}
