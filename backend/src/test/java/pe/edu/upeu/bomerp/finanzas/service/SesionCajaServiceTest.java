package pe.edu.upeu.bomerp.finanzas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.exception.SolvenciaInsuficienteException;
import pe.edu.upeu.bomerp.finanzas.dto.AperturaSesionRequest;
import pe.edu.upeu.bomerp.finanzas.dto.CierreSesionRequest;
import pe.edu.upeu.bomerp.finanzas.dto.MovimientoCajaRequest;
import pe.edu.upeu.bomerp.finanzas.dto.MovimientoCajaResponse;
import pe.edu.upeu.bomerp.finanzas.dto.SesionCajaDetalleResponse;
import pe.edu.upeu.bomerp.finanzas.dto.SesionCajaResponse;
import pe.edu.upeu.bomerp.finanzas.entity.CategoriaFinanciera;
import pe.edu.upeu.bomerp.finanzas.entity.EstadoCaja;
import pe.edu.upeu.bomerp.finanzas.entity.MetodoPago;
import pe.edu.upeu.bomerp.finanzas.entity.TipoFinanciero;
import pe.edu.upeu.bomerp.finanzas.repository.CategoriaFinancieraRepository;
import pe.edu.upeu.bomerp.finanzas.repository.MovimientoCajaRepository;
import pe.edu.upeu.bomerp.finanzas.repository.SesionCajaRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("h2")
@TestPropertySource(properties = "app.init.demo-data=false")
class SesionCajaServiceTest {

    @Autowired
    private SesionCajaService sesionCajaService;

    @Autowired
    private SesionCajaRepository sesionRepository;

    @Autowired
    private MovimientoCajaRepository movimientoRepository;

    @Autowired
    private CategoriaFinancieraRepository categoriaRepository;

    @Autowired
    private pe.edu.upeu.bomerp.finanzas.repository.DenominacionMonedaRepository denominacionRepository;

    private Long categoriaVentasId;
    private Long categoriaGastosId;

    @BeforeEach
    void setUp() {
        CategoriaFinanciera catVentas = new CategoriaFinanciera();
        catVentas.setNombre("Venta Mostrador Derivados Cacao");
        catVentas.setTipoDefault(TipoFinanciero.INGRESO);
        categoriaVentasId = categoriaRepository.save(catVentas).getId();

        CategoriaFinanciera catGastos = new CategoriaFinanciera();
        catGastos.setNombre("Gastos Menores / Transporte");
        catGastos.setTipoDefault(TipoFinanciero.EGRESO);
        categoriaGastosId = categoriaRepository.save(catGastos).getId();
    }

    @Nested
    @DisplayName("Pruebas de Ciclo de Vida: Apertura y Cierre")
    class CicloVidaTests {

        @Test
        @DisplayName("Debe abrir turno de caja exitosamente con saldo inicial")
        void debeAbrirSesionExitosamente() {
            AperturaSesionRequest req = AperturaSesionRequest.builder()
                    .usuarioId(101L)
                    .cajero("Brandon Ccalla")
                    .saldoInicial(new BigDecimal("250.00"))
                    .codigoSesion("SES-TEST-001")
                    .build();

            SesionCajaResponse resp = sesionCajaService.abrirSesion(req);

            assertThat(resp.getId()).isNotNull();
            assertThat(resp.getCodigoSesion()).isEqualTo("SES-TEST-001");
            assertThat(resp.getEstado()).isEqualTo(EstadoCaja.ABIERTA);
            assertThat(resp.getSaldoInicial()).isEqualByComparingTo("250.00");
            assertThat(resp.getSaldoEsperadoEfectivo()).isEqualByComparingTo("250.00");
        }

        @Test
        @DisplayName("Debe rechazar apertura si el usuario ya tiene un turno abierto (RN-FIN-02)")
        void debeRechazarAperturaDuplicada() {
            AperturaSesionRequest req = AperturaSesionRequest.builder()
                    .usuarioId(102L)
                    .cajero("Brandon Ccalla")
                    .saldoInicial(new BigDecimal("100.00"))
                    .build();

            sesionCajaService.abrirSesion(req);

            assertThatThrownBy(() -> sesionCajaService.abrirSesion(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("RN-FIN-02");
        }

        @Test
        @DisplayName("Debe cerrar turno de caja y calcular diferencia de arqueo físico")
        void debeCerrarSesionYCalcularDiferencia() {
            AperturaSesionRequest req = AperturaSesionRequest.builder()
                    .usuarioId(103L)
                    .cajero("Brandon Ccalla")
                    .saldoInicial(new BigDecimal("200.00"))
                    .build();
            SesionCajaResponse sesion = sesionCajaService.abrirSesion(req);

            // Arqueo físico: cajero cuenta S/ 205.00 (sobrante de S/ 5.00)
            CierreSesionRequest cierreReq = CierreSesionRequest.builder()
                    .saldoRealEfectivo(new BigDecimal("205.00"))
                    .observaciones("Turno cerrado con arqueo conforme.")
                    .build();

            SesionCajaResponse cerrada = sesionCajaService.cerrarSesion(sesion.getId(), cierreReq);

            assertThat(cerrada.getEstado()).isEqualTo(EstadoCaja.CERRADA);
            assertThat(cerrada.getSaldoRealEfectivo()).isEqualByComparingTo("205.00");
            assertThat(cerrada.getDiferencia()).isEqualByComparingTo("5.00");
        }

        @Test
        @DisplayName("Debe cerrar turno con desglose de billetes y monedas M:N y persistir DetalleArqueoEfectivo")
        void debeCerrarSesionConDesgloseDeDenominacionesYPersistirDetalleArqueo() {
            var denom100 = denominacionRepository.save(pe.edu.upeu.bomerp.finanzas.entity.DenominacionMoneda.builder()
                    .valorFacial(new BigDecimal("100.00"))
                    .tipo(pe.edu.upeu.bomerp.finanzas.entity.TipoDenominacion.BILLETE)
                    .orden(1)
                    .activo(true)
                    .build());

            var denom50 = denominacionRepository.save(pe.edu.upeu.bomerp.finanzas.entity.DenominacionMoneda.builder()
                    .valorFacial(new BigDecimal("50.00"))
                    .tipo(pe.edu.upeu.bomerp.finanzas.entity.TipoDenominacion.BILLETE)
                    .orden(2)
                    .activo(true)
                    .build());

            AperturaSesionRequest req = AperturaSesionRequest.builder()
                    .usuarioId(109L)
                    .cajero("Brandon Ccalla")
                    .saldoInicial(new BigDecimal("150.00"))
                    .build();
            SesionCajaResponse sesion = sesionCajaService.abrirSesion(req);

            var item1 = pe.edu.upeu.bomerp.finanzas.dto.DesgloseEfectivoItemDto.builder()
                    .denominacionId(denom100.getId())
                    .valorFacial(new BigDecimal("100.00"))
                    .tipo(pe.edu.upeu.bomerp.finanzas.entity.TipoDenominacion.BILLETE)
                    .cantidad(1)
                    .subtotal(new BigDecimal("100.00"))
                    .build();

            var item2 = pe.edu.upeu.bomerp.finanzas.dto.DesgloseEfectivoItemDto.builder()
                    .denominacionId(denom50.getId())
                    .valorFacial(new BigDecimal("50.00"))
                    .tipo(pe.edu.upeu.bomerp.finanzas.entity.TipoDenominacion.BILLETE)
                    .cantidad(1)
                    .subtotal(new BigDecimal("50.00"))
                    .build();

            CierreSesionRequest cierreReq = CierreSesionRequest.builder()
                    .saldoRealEfectivo(new BigDecimal("150.00"))
                    .observaciones("Cierre arqueo conforme con desglose")
                    .desglose(java.util.List.of(item1, item2))
                    .build();

            SesionCajaResponse cerrada = sesionCajaService.cerrarSesion(sesion.getId(), cierreReq);
            assertThat(cerrada.getEstado()).isEqualTo(EstadoCaja.CERRADA);
            assertThat(cerrada.getSaldoRealEfectivo()).isEqualByComparingTo("150.00");
            assertThat(cerrada.getDiferencia()).isEqualByComparingTo("0.00");

            SesionCajaDetalleResponse detalle = sesionCajaService.obtenerDetalle(sesion.getId());
            assertThat(detalle.getDetallesArqueo()).hasSize(2);
            assertThat(detalle.getDetallesArqueo()).extracting("subtotal")
                    .contains(new BigDecimal("100.00"), new BigDecimal("50.00"));
        }
    }

    @Nested
    @DisplayName("Pruebas de Regla de Negocio RN-FIN-01 (Control de Liquidez)")
    class ReglaSolvenciaTests {

        @Test
        @DisplayName("RN-FIN-01: Debe permitir egreso en efectivo si el monto <= saldo disponible")
        void debePermitirEgresoConSaldoSuficiente() {
            AperturaSesionRequest req = AperturaSesionRequest.builder()
                    .usuarioId(104L)
                    .cajero("Brandon Ccalla")
                    .saldoInicial(new BigDecimal("300.00"))
                    .build();
            SesionCajaResponse sesion = sesionCajaService.abrirSesion(req);

            MovimientoCajaRequest mov = MovimientoCajaRequest.builder()
                    .tipo(TipoFinanciero.EGRESO)
                    .metodoPago(MetodoPago.EFECTIVO)
                    .monto(new BigDecimal("80.00"))
                    .descripcion("Compra de cinta para embalaje de chocolate")
                    .categoriaFinancieraId(categoriaGastosId)
                    .build();

            MovimientoCajaResponse movResp = sesionCajaService.registrarMovimiento(sesion.getId(), mov);

            assertThat(movResp.getId()).isNotNull();

            SesionCajaDetalleResponse detalle = sesionCajaService.obtenerDetalle(sesion.getId());
            assertThat(detalle.getTotalEgresosEfectivo()).isEqualByComparingTo("80.00");
            assertThat(detalle.getSaldoEsperadoEfectivo()).isEqualByComparingTo("220.00");
        }

        @Test
        @DisplayName("RN-FIN-01 CRÍTICO: Debe bloquear egreso en efectivo mayor al disponible y lanzar SolvenciaInsuficienteException")
        void debeBloquearEgresoQueSuperaSaldoDisponible() {
            AperturaSesionRequest req = AperturaSesionRequest.builder()
                    .usuarioId(105L)
                    .cajero("Brandon Ccalla")
                    .saldoInicial(new BigDecimal("150.00"))
                    .build();
            SesionCajaResponse sesion = sesionCajaService.abrirSesion(req);

            // Intento de retiro de S/ 200.00 (mayor a S/ 150.00)
            MovimientoCajaRequest movInvalido = MovimientoCajaRequest.builder()
                    .tipo(TipoFinanciero.EGRESO)
                    .metodoPago(MetodoPago.EFECTIVO)
                    .monto(new BigDecimal("200.00"))
                    .descripcion("Retiro no autorizado sin liquidez")
                    .categoriaFinancieraId(categoriaGastosId)
                    .build();

            assertThatThrownBy(() -> sesionCajaService.registrarMovimiento(sesion.getId(), movInvalido))
                    .isInstanceOf(SolvenciaInsuficienteException.class)
                    .hasMessageContaining("RN-FIN-01")
                    .hasMessageContaining("excede el saldo en efectivo disponible");

            // Verificar que el saldo de la caja quedó intacto en S/ 150.00 (Rollback atómico)
            SesionCajaDetalleResponse detalle = sesionCajaService.obtenerDetalle(sesion.getId());
            assertThat(detalle.getSaldoEsperadoEfectivo()).isEqualByComparingTo("150.00");
            assertThat(detalle.getTotalEgresosEfectivo()).isEqualByComparingTo("0.00");
            assertThat(detalle.getMovimientos()).isEmpty();
        }
    }
}
