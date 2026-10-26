package pe.edu.upeu.bomerp.finanzas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.catalogo.producto.dto.ProductoResponse;
import pe.edu.upeu.bomerp.catalogo.producto.service.ProductoService;
import pe.edu.upeu.bomerp.exception.SolvenciaInsuficienteException;
import pe.edu.upeu.bomerp.finanzas.dto.CajaChicaResponse;
import pe.edu.upeu.bomerp.finanzas.dto.DetalleRendicionRequest;
import pe.edu.upeu.bomerp.finanzas.dto.RendicionCajaChicaRequest;
import pe.edu.upeu.bomerp.finanzas.dto.RendicionCajaChicaResponse;
import pe.edu.upeu.bomerp.finanzas.entity.CajaChica;
import pe.edu.upeu.bomerp.finanzas.entity.DetalleRendicionCajaChica;
import pe.edu.upeu.bomerp.finanzas.entity.RendicionCajaChica;
import pe.edu.upeu.bomerp.finanzas.repository.CajaChicaRepository;
import pe.edu.upeu.bomerp.finanzas.repository.DetalleRendicionCajaChicaRepository;
import pe.edu.upeu.bomerp.finanzas.repository.RendicionCajaChicaRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "app.init.demo-data=false")
class RendicionCajaChicaServiceTest {

    @Autowired
    private RendicionCajaChicaService rendicionService;

    @Autowired
    private CajaChicaRepository cajaChicaRepository;

    @Autowired
    private RendicionCajaChicaRepository rendicionRepository;

    @Autowired
    private DetalleRendicionCajaChicaRepository detalleRepository;

    @Autowired
    private pe.edu.upeu.bomerp.finanzas.repository.CentroCostoRepository centroCostoRepository;

    @Autowired
    private pe.edu.upeu.bomerp.finanzas.repository.AsignacionCajaCentroCostoRepository asignacionRepository;

    @MockitoBean
    private ProductoService productoService;

    private Long cajaChicaId;

    @BeforeEach
    void setUp() {
        // Mock de comunicación inter-módulo con catalogo
        when(productoService.obtener(anyLong())).thenReturn(ProductoResponse.builder()
                .id(100L)
                .nombre("Útiles de Oficina")
                .precio(new BigDecimal("25.00"))
                .build());

        // Obtener o crear la caja chica inicial con saldo disponible S/ 1000.00
        CajaChica caja = cajaChicaRepository.findByCodigo("CC-001")
                .orElseGet(() -> cajaChicaRepository.save(CajaChica.builder()
                        .codigo("CC-001")
                        .responsable("Juan Pérez")
                        .limiteMaximo(new BigDecimal("2000.00"))
                        .saldoDisponible(new BigDecimal("1000.00"))
                        .build()));
        caja.setSaldoDisponible(new BigDecimal("1000.00"));
        cajaChicaRepository.save(caja);
        cajaChicaId = caja.getId();
    }

    @Nested
    @DisplayName("1. Cabecera-Detalle y Cálculo de Totales")
    class CalculoTotalesTests {

        @Test
        @DisplayName("Debe calcular correctamente los subtotales de detalle y el total acumulado en la cabecera")
        void debeCalcularTotalesCorrectamente() {
            // Arrange: 2 ítems en el detalle
            // Item 1: Cantidad 4 * S/ 50.00 = S/ 200.00
            // Item 2: Cantidad 2 * S/ 100.00 = S/ 200.00
            // Total esperado: S/ 400.00
            DetalleRendicionRequest item1 = DetalleRendicionRequest.builder()
                    .concepto("Hojas Bond A4")
                    .cantidad(4)
                    .precioUnitario(new BigDecimal("50.00"))
                    .productoId(100L) // Cruza módulo catalogo
                    .build();

            DetalleRendicionRequest item2 = DetalleRendicionRequest.builder()
                    .concepto("Cartuchos de Tinta")
                    .cantidad(2)
                    .precioUnitario(new BigDecimal("100.00"))
                    .build();

            RendicionCajaChicaRequest request = RendicionCajaChicaRequest.builder()
                    .codigo("REND-2026-01")
                    .responsable("Carlos M.")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(item1, item2))
                    .build();

            // Act
            RendicionCajaChicaResponse response = rendicionService.registrarRendicion(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getMontoTotal()).isEqualByComparingTo(new BigDecimal("400.00"));
            assertThat(response.getDetalles()).hasSize(2);
            assertThat(response.getDetalles().get(0).getSubtotal()).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(response.getDetalles().get(1).getSubtotal()).isEqualByComparingTo(new BigDecimal("200.00"));
        }
    }

    @Nested
    @DisplayName("2. Regla de Negocio RN-FIN-01 (Solvencia de Caja Chica / Control de Liquidez)")
    class ReglaSolvenciaTests {

        @Test
        @DisplayName("RN-FIN-01: Debe permitir el registro si el monto total (S/ 600.00) es menor o igual al saldo disponible (S/ 1000.00)")
        void debePermitirRegistroCuandoHaySolvencia() {
            DetalleRendicionRequest item = DetalleRendicionRequest.builder()
                    .concepto("Mantenimiento de Equipo")
                    .cantidad(3)
                    .precioUnitario(new BigDecimal("200.00")) // Total = 600.00
                    .build();

            RendicionCajaChicaRequest request = RendicionCajaChicaRequest.builder()
                    .codigo("REND-2026-02")
                    .responsable("Ana Torres")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(item))
                    .build();

            RendicionCajaChicaResponse response = rendicionService.registrarRendicion(request);

            assertThat(response.getMontoTotal()).isEqualByComparingTo(new BigDecimal("600.00"));

            CajaChica cajaActualizada = cajaChicaRepository.findById(cajaChicaId).orElseThrow();
            // Saldo disponible disminuye de 1000.00 a 400.00
            assertThat(cajaActualizada.getSaldoDisponible()).isEqualByComparingTo(new BigDecimal("400.00"));
        }

        @Test
        @DisplayName("RN-FIN-01: Debe lanzar SolvenciaInsuficienteException si el monto total (S/ 1500.00) supera el saldo disponible (S/ 1000.00)")
        void debeLanzarExcepcionCuandoMontoExcedeSaldoDisponible() {
            DetalleRendicionRequest item = DetalleRendicionRequest.builder()
                    .concepto("Compra de Laptop Repuesto")
                    .cantidad(1)
                    .precioUnitario(new BigDecimal("1500.00")) // Total = 1500.00 > 1000.00
                    .build();

            RendicionCajaChicaRequest request = RendicionCajaChicaRequest.builder()
                    .codigo("REND-EXCESO-01")
                    .responsable("Ana Torres")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(item))
                    .build();

            assertThatThrownBy(() -> rendicionService.registrarRendicion(request))
                    .isInstanceOf(SolvenciaInsuficienteException.class)
                    .hasMessageContaining("RN-FIN-01")
                    .hasMessageContaining("supera la solvencia/saldo disponible");
        }
    }

    @Nested
    @DisplayName("3. Transacción Atómica (Caso Éxito y Rollback)")
    class TransaccionAtomicaTests {

        @Test
        @DisplayName("Caso Éxito: Cabecera y detalles se persisten atómicamente y el saldo de la caja se actualiza")
        void casoExitoPersistenciaAtomica() {
            // Estado antes
            long rendicionesAntes = rendicionRepository.count();
            long detallesAntes = detalleRepository.count();
            CajaChica cajaAntes = cajaChicaRepository.findById(cajaChicaId).orElseThrow();
            BigDecimal saldoAntes = cajaAntes.getSaldoDisponible();

            assertThat(saldoAntes).isEqualByComparingTo(new BigDecimal("1000.00"));

            // Operación: Registrar rendición por S/ 300.00
            DetalleRendicionRequest d1 = DetalleRendicionRequest.builder()
                    .concepto("Pasajes urbanos")
                    .cantidad(5)
                    .precioUnitario(new BigDecimal("20.00")) // S/ 100.00
                    .build();

            DetalleRendicionRequest d2 = DetalleRendicionRequest.builder()
                    .concepto("Almuerzo de trabajo")
                    .cantidad(2)
                    .precioUnitario(new BigDecimal("100.00")) // S/ 200.00
                    .build();

            RendicionCajaChicaRequest request = RendicionCajaChicaRequest.builder()
                    .codigo("REND-EXITO-01")
                    .responsable("Pedro Picapiedra")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(d1, d2))
                    .build();

            RendicionCajaChicaResponse resp = rendicionService.registrarRendicion(request);

            // Estado después
            assertThat(rendicionRepository.count()).isEqualTo(rendicionesAntes + 1);
            assertThat(detalleRepository.count()).isEqualTo(detallesAntes + 2);

            CajaChica cajaDespues = cajaChicaRepository.findById(cajaChicaId).orElseThrow();
            assertThat(cajaDespues.getSaldoDisponible()).isEqualByComparingTo(new BigDecimal("700.00"));

            RendicionCajaChica rendicionBd = rendicionRepository.findById(resp.getId()).orElseThrow();
            assertThat(rendicionBd.getMontoTotal()).isEqualByComparingTo(new BigDecimal("300.00"));
            assertThat(rendicionBd.getDetalles()).hasSize(2);
        }

        @Test
        @DisplayName("Caso Rollback: Si falla por la regla RN-FIN-01, se revierte toda la operación (ni cabecera ni detalles persisten)")
        void casoRollbackTransaccional() {
            // Estado antes
            long rendicionesAntes = rendicionRepository.count();
            long detallesAntes = detalleRepository.count();
            BigDecimal saldoAntes = cajaChicaRepository.findById(cajaChicaId).orElseThrow().getSaldoDisponible();

            // Operación que viola la regla RN-FIN-01 (Monto S/ 1200.00 > Saldo S/ 1000.00)
            DetalleRendicionRequest d1 = DetalleRendicionRequest.builder()
                    .concepto("Gasto excesivo item 1")
                    .cantidad(1)
                    .precioUnitario(new BigDecimal("600.00"))
                    .build();

            DetalleRendicionRequest d2 = DetalleRendicionRequest.builder()
                    .concepto("Gasto excesivo item 2")
                    .cantidad(1)
                    .precioUnitario(new BigDecimal("600.00"))
                    .build();

            RendicionCajaChicaRequest request = RendicionCajaChicaRequest.builder()
                    .codigo("REND-FAIL-01")
                    .responsable("Pedro Picapiedra")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(d1, d2))
                    .build();

            assertThatThrownBy(() -> rendicionService.registrarRendicion(request))
                    .isInstanceOf(SolvenciaInsuficienteException.class);

            // Estado después: Estado intocado (Rollback verificado)
            assertThat(rendicionRepository.count()).isEqualTo(rendicionesAntes);
            assertThat(detalleRepository.count()).isEqualTo(detallesAntes);
            BigDecimal saldoDespues = cajaChicaRepository.findById(cajaChicaId).orElseThrow().getSaldoDisponible();
            assertThat(saldoDespues).isEqualByComparingTo(saldoAntes);
        }
    }

    @Nested
    @DisplayName("4. Filtros Combinados y Ordenamiento")
    class FiltrosCombinadosYOrdenamientoTests {

        @BeforeEach
        void poblarDatosParaFiltros() {
            // Rendición 1: Carlos M. - S/ 100.00
            rendicionService.registrarRendicion(RendicionCajaChicaRequest.builder()
                    .codigo("REND-FILT-01")
                    .responsable("Carlos Mendoza")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(DetalleRendicionRequest.builder()
                            .concepto("Gasto 1")
                            .cantidad(1)
                            .precioUnitario(new BigDecimal("100.00"))
                            .build()))
                    .build());

            // Rendición 2: Carlos M. - S/ 300.00
            rendicionService.registrarRendicion(RendicionCajaChicaRequest.builder()
                    .codigo("REND-FILT-02")
                    .responsable("Carlos Mendoza")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(DetalleRendicionRequest.builder()
                            .concepto("Gasto 2")
                            .cantidad(3)
                            .precioUnitario(new BigDecimal("100.00"))
                            .build()))
                    .build());

            // Rendición 3: Beatriz Flores - S/ 200.00
            rendicionService.registrarRendicion(RendicionCajaChicaRequest.builder()
                    .codigo("REND-FILT-03")
                    .responsable("Beatriz Flores")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(DetalleRendicionRequest.builder()
                            .concepto("Gasto 3")
                            .cantidad(2)
                            .precioUnitario(new BigDecimal("100.00"))
                            .build()))
                    .build());
        }

        @Test
        @DisplayName("Combinación 1: Filtrar por responsable 'Carlos' + estado APROBADO ordenado por montoTotal DESC")
        void filtroCombinado1_responsableYEstado_ordenDesc() {
            org.springframework.data.domain.Pageable pageable =
                    org.springframework.data.domain.PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("montoTotal").descending());

            org.springframework.data.domain.Page<RendicionCajaChicaResponse> resultado =
                    rendicionService.buscarConFiltros(null, pe.edu.upeu.bomerp.finanzas.entity.EstadoRendicion.APROBADO, "Carlos", null, null, null, null, pageable);

            assertThat(resultado.getContent()).hasSize(2);
            assertThat(resultado.getContent().get(0).getCodigo()).isEqualTo("REND-FILT-02");
            assertThat(resultado.getContent().get(0).getMontoTotal()).isEqualByComparingTo(new BigDecimal("300.00"));
            assertThat(resultado.getContent().get(1).getCodigo()).isEqualTo("REND-FILT-01");
            assertThat(resultado.getContent().get(1).getMontoTotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Combinación 2: Filtrar por cajaChicaId + rango de montos (S/ 150.00 a S/ 350.00) ordenado por montoTotal ASC")
        void filtroCombinado2_cajaYRangoMontos_ordenAsc() {
            org.springframework.data.domain.Pageable pageable =
                    org.springframework.data.domain.PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("montoTotal").ascending());

            org.springframework.data.domain.Page<RendicionCajaChicaResponse> resultado =
                    rendicionService.buscarConFiltros(cajaChicaId, null, null, null, null, new BigDecimal("150.00"), new BigDecimal("350.00"), pageable);

            assertThat(resultado.getContent()).hasSize(2);
            assertThat(resultado.getContent().get(0).getCodigo()).isEqualTo("REND-FILT-03"); // 200.00
            assertThat(resultado.getContent().get(1).getCodigo()).isEqualTo("REND-FILT-02"); // 300.00
        }

        @Test
        @DisplayName("Combinación 3: Filtrar por rango de fechas + estado APROBADO ordenado por responsable ASC")
        void filtroCombinado3_fechasYEstado_ordenResponsableAsc() {
            org.springframework.data.domain.Pageable pageable =
                    org.springframework.data.domain.PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("responsable").ascending());

            java.time.LocalDate hoy = java.time.LocalDate.now();
            org.springframework.data.domain.Page<RendicionCajaChicaResponse> resultado =
                    rendicionService.buscarConFiltros(null, pe.edu.upeu.bomerp.finanzas.entity.EstadoRendicion.APROBADO, null, hoy.minusDays(1), hoy.plusDays(1), null, null, pageable);

            assertThat(resultado.getContent()).hasSize(3);
            assertThat(resultado.getContent().get(0).getResponsable()).isEqualTo("Beatriz Flores");
            assertThat(resultado.getContent().get(1).getResponsable()).isEqualTo("Carlos Mendoza");
        }

        @Test
        @DisplayName("Cambio de orden: Misma consulta ordenada DESC vs ASC por montoTotal")
        void cambioDeOrden_montoTotal() {
            org.springframework.data.domain.Pageable pageDesc =
                    org.springframework.data.domain.PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("montoTotal").descending());
            org.springframework.data.domain.Pageable pageAsc =
                    org.springframework.data.domain.PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("montoTotal").ascending());

            org.springframework.data.domain.Page<RendicionCajaChicaResponse> resDesc =
                    rendicionService.buscarConFiltros(cajaChicaId, null, null, null, null, null, null, pageDesc);
            org.springframework.data.domain.Page<RendicionCajaChicaResponse> resAsc =
                    rendicionService.buscarConFiltros(cajaChicaId, null, null, null, null, null, null, pageAsc);

            assertThat(resDesc.getContent().get(0).getMontoTotal()).isEqualByComparingTo(new BigDecimal("300.00"));
            assertThat(resAsc.getContent().get(0).getMontoTotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }

    @Nested
    @DisplayName("5. Proyección de Resumen (Expresión de Constructor JPQL)")
    class ProyeccionResumenTests {

        @Test
        @DisplayName("Debe instanciar directamente RendicionResumenDto con campos y conteo de detalles (SIZE)")
        void debeObtenerResumenProyectado() {
            rendicionService.registrarRendicion(RendicionCajaChicaRequest.builder()
                    .codigo("REND-PROY-01")
                    .responsable("Elena Ramos")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(
                            DetalleRendicionRequest.builder().concepto("D1").cantidad(1).precioUnitario(new BigDecimal("50.00")).build(),
                            DetalleRendicionRequest.builder().concepto("D2").cantidad(1).precioUnitario(new BigDecimal("50.00")).build()
                    ))
                    .build());

            List<pe.edu.upeu.bomerp.finanzas.dto.RendicionResumenDto> resumenes = rendicionService.obtenerResumen(cajaChicaId);

            assertThat(resumenes).isNotEmpty();
            pe.edu.upeu.bomerp.finanzas.dto.RendicionResumenDto item = resumenes.stream()
                    .filter(r -> "REND-PROY-01".equals(r.getCodigo()))
                    .findFirst()
                    .orElseThrow();

            assertThat(item.getResponsable()).isEqualTo("Elena Ramos");
            assertThat(item.getMontoTotal()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(item.getCodigoCajaChica()).isEqualTo("CC-001");
            assertThat(item.getCantidadDetalles()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("6. Reporte Agregado (Caso con resultados y caso sin resultados: 0 en vez de null)")
    class ReporteAgregadoTests {

        @Test
        @DisplayName("Caso CON resultados: Devuelve suma, promedio, max, min y conteo calculados correctamente")
        void reporteConResultados() {
            rendicionService.registrarRendicion(RendicionCajaChicaRequest.builder()
                    .codigo("REND-REP-01")
                    .responsable("Rendidor 1")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(DetalleRendicionRequest.builder().concepto("G1").cantidad(1).precioUnitario(new BigDecimal("200.00")).build()))
                    .build());

            rendicionService.registrarRendicion(RendicionCajaChicaRequest.builder()
                    .codigo("REND-REP-02")
                    .responsable("Rendidor 2")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(DetalleRendicionRequest.builder().concepto("G2").cantidad(1).precioUnitario(new BigDecimal("400.00")).build()))
                    .build());

            pe.edu.upeu.bomerp.finanzas.dto.ReporteRendicionesAgregadoDto reporte = rendicionService.obtenerReporteAgregado(cajaChicaId);

            assertThat(reporte).isNotNull();
            assertThat(reporte.getTotalMonto()).isEqualByComparingTo(new BigDecimal("600.00"));
            assertThat(reporte.getPromedioMonto()).isEqualTo(300.0);
            assertThat(reporte.getMontoMaximo()).isEqualByComparingTo(new BigDecimal("400.00"));
            assertThat(reporte.getMontoMinimo()).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(reporte.getCantidadRendiciones()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Caso SIN resultados: Devuelve monto y promedio en 0 (0.00, 0.0, 0) y NUNCA null")
        void reporteSinResultados_devuelveCerosNoNull() {
            // Creamos una nueva caja chica vacía (sin rendiciones asociadas)
            pe.edu.upeu.bomerp.finanzas.dto.CajaChicaResponse nuevaCaja =
                    rendicionService.crearCajaChica("CC-VACIA", "Admin", new BigDecimal("1000.00"), new BigDecimal("1000.00"));

            pe.edu.upeu.bomerp.finanzas.dto.ReporteRendicionesAgregadoDto reporte =
                    rendicionService.obtenerReporteAgregado(nuevaCaja.getId());

            assertThat(reporte).isNotNull();
            // Verificación estricta: NO es null y sus valores son 0
            assertThat(reporte.getTotalMonto()).isNotNull().isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(reporte.getPromedioMonto()).isNotNull().isEqualTo(0.0);
            assertThat(reporte.getMontoMaximo()).isNotNull().isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(reporte.getMontoMinimo()).isNotNull().isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(reporte.getCantidadRendiciones()).isNotNull().isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("7. Relación M:N Asignación Presupuestal por Centro de Costo (RN-FIN-06)")
    class ControlPresupuestalCentroCostoTests {

        @Test
        @DisplayName("RN-FIN-06: Debe descontar saldo presupuestal por centro de costo al registrar rendición")
        void debeDescontarSaldoPresupuestalPorCentroCosto() {
            var cc = centroCostoRepository.save(pe.edu.upeu.bomerp.finanzas.entity.CentroCosto.builder()
                    .codigoCc("CC-TEST-01")
                    .nombreArea("Área Prueba")
                    .activo(true)
                    .build());

            rendicionService.asignarPresupuestoCentroCosto(cajaChicaId, cc.getId(), new BigDecimal("300.00"));

            DetalleRendicionRequest item = DetalleRendicionRequest.builder()
                    .concepto("Insumos de Prueba")
                    .cantidad(2)
                    .precioUnitario(new BigDecimal("50.00")) // S/ 100.00
                    .centroCostoId(cc.getId())
                    .build();

            RendicionCajaChicaRequest req = RendicionCajaChicaRequest.builder()
                    .codigo("REND-CC-TEST-01")
                    .responsable("Juan Pérez")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(item))
                    .build();

            rendicionService.registrarRendicion(req);

            var presupuestos = rendicionService.listarPresupuestosPorCaja(cajaChicaId);
            assertThat(presupuestos).anyMatch(p -> p.getCodigoCc().equals("CC-TEST-01") &&
                    p.getSaldoDisponible().compareTo(new BigDecimal("200.00")) == 0);
        }

        @Test
        @DisplayName("RN-FIN-06: Debe bloquear gasto y lanzar SolvenciaInsuficienteException si excede el saldo del centro de costo")
        void debeBloquearGastoQueExcedePresupuestoCentroCosto() {
            var cc = centroCostoRepository.save(pe.edu.upeu.bomerp.finanzas.entity.CentroCosto.builder()
                    .codigoCc("CC-TEST-02")
                    .nombreArea("Área Prueba 2")
                    .activo(true)
                    .build());

            rendicionService.asignarPresupuestoCentroCosto(cajaChicaId, cc.getId(), new BigDecimal("80.00"));

            DetalleRendicionRequest item = DetalleRendicionRequest.builder()
                    .concepto("Compra que excede techo de centro de costo")
                    .cantidad(1)
                    .precioUnitario(new BigDecimal("100.00")) // S/ 100.00 > S/ 80.00 disponible
                    .centroCostoId(cc.getId())
                    .build();

            RendicionCajaChicaRequest req = RendicionCajaChicaRequest.builder()
                    .codigo("REND-CC-EXCESO-01")
                    .responsable("Juan Pérez")
                    .cajaChicaId(cajaChicaId)
                    .detalles(List.of(item))
                    .build();

            assertThatThrownBy(() -> rendicionService.registrarRendicion(req))
                    .isInstanceOf(SolvenciaInsuficienteException.class)
                    .hasMessageContaining("RN-FIN-06")
                    .hasMessageContaining("excede el saldo presupuestal disponible");
        }
    }
}
