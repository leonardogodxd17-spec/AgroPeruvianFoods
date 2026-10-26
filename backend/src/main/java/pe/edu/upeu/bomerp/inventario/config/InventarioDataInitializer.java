package pe.edu.upeu.bomerp.inventario.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import pe.edu.upeu.bomerp.catalogo.producto.entity.Producto;
import pe.edu.upeu.bomerp.catalogo.producto.repository.ProductoRepository;
import pe.edu.upeu.bomerp.inventario.dto.LoteCreateDto;
import pe.edu.upeu.bomerp.inventario.repository.LoteRepository;
import pe.edu.upeu.bomerp.inventario.service.InventarioService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Configuration
@Order(10) // Ejecutar después del CatalogoDataInitializer
@ConditionalOnProperty(name = "app.init.demo-data", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class InventarioDataInitializer implements CommandLineRunner {

    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;

    @Override
    public void run(String... args) {
        if (loteRepository.count() == 0) {
            log.info("Inicializando lotes de inventario FEFO para Agro Peruvian Foods...");

            List<Producto> productos = productoRepository.findAll();
            if (productos.isEmpty()) {
                log.warn("No hay productos en catálogo para vincular lotes demo.");
                return;
            }

            LocalDate hoy = LocalDate.now();

            for (Producto p : productos) {
                if (p.getNombre().contains("Aceite de Coco Virgen 460ml")) {
                    // Lote 1: Próximo a vencer (18 días) -> Debe consumirse PRIMERO por FEFO
                    inventarioService.registrarLote(LoteCreateDto.builder()
                            .codigoLote("LOT-COCO460-2026-A1")
                            .productoId(p.getId())
                            .fechaFabricacion(hoy.minusMonths(5))
                            .fechaVencimiento(hoy.plusDays(18))
                            .stockInicial(new BigDecimal("20.00"))
                            .costoUnitario(new BigDecimal("22.00"))
                            .observaciones("Lote con vencimiento cercano. Prioridad FEFO de venta.")
                            .build());

                    // Lote 2: Fresco (vence en 8 meses) -> Debe consumirse DESPUÉS
                    inventarioService.registrarLote(LoteCreateDto.builder()
                            .codigoLote("LOT-COCO460-2026-B2")
                            .productoId(p.getId())
                            .fechaFabricacion(hoy.minusWeeks(2))
                            .fechaVencimiento(hoy.plusMonths(8))
                            .stockInicial(new BigDecimal("60.00"))
                            .costoUnitario(new BigDecimal("23.50"))
                            .observaciones("Lote fresco recién acondicionado.")
                            .build());
                } else if (p.getNombre().contains("Cacao Grano Entero 500g")) {
                    // Lote 1: Vence en 45 días
                    inventarioService.registrarLote(LoteCreateDto.builder()
                            .codigoLote("LOT-CACAO500-2026-01")
                            .productoId(p.getId())
                            .fechaFabricacion(hoy.minusMonths(4))
                            .fechaVencimiento(hoy.plusDays(45))
                            .stockInicial(new BigDecimal("30.00"))
                            .costoUnitario(new BigDecimal("9.00"))
                            .observaciones("Cacao grano entero lote 01.")
                            .build());

                    // Lote 2: Vence en 10 meses
                    inventarioService.registrarLote(LoteCreateDto.builder()
                            .codigoLote("LOT-CACAO500-2026-02")
                            .productoId(p.getId())
                            .fechaFabricacion(hoy.minusDays(5))
                            .fechaVencimiento(hoy.plusMonths(10))
                            .stockInicial(new BigDecimal("60.00"))
                            .costoUnitario(new BigDecimal("9.50"))
                            .observaciones("Cacao grano entero lote 02 selección fresca.")
                            .build());
                } else if (p.getNombre().contains("Coco Rallado Doypack 250g")) {
                    // Lote 1: Próximo a vencer (12 días)
                    inventarioService.registrarLote(LoteCreateDto.builder()
                            .codigoLote("LOT-COCORALL-2026-01")
                            .productoId(p.getId())
                            .fechaFabricacion(hoy.minusMonths(6))
                            .fechaVencimiento(hoy.plusDays(12))
                            .stockInicial(new BigDecimal("25.00"))
                            .costoUnitario(new BigDecimal("5.00"))
                            .observaciones("Coco rallado lote con menor vida útil.")
                            .build());

                    // Lote 2: Vence en 7 meses
                    inventarioService.registrarLote(LoteCreateDto.builder()
                            .codigoLote("LOT-COCORALL-2026-02")
                            .productoId(p.getId())
                            .fechaFabricacion(hoy.minusDays(10))
                            .fechaVencimiento(hoy.plusMonths(7))
                            .stockInicial(new BigDecimal("50.00"))
                            .costoUnitario(new BigDecimal("5.20"))
                            .observaciones("Coco rallado lote estándar hebra larga.")
                            .build());
                }
            }

            log.info("Lotes de inventario FEFO inicializados satisfactoriamente.");
        }
    }
}
