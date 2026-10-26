package pe.edu.upeu.bomerp.inventario.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.bomerp.inventario.dto.DescuentoFefoRequest;
import pe.edu.upeu.bomerp.inventario.dto.DescuentoFefoResponse;
import pe.edu.upeu.bomerp.inventario.dto.LoteCreateDto;
import pe.edu.upeu.bomerp.inventario.dto.LoteDto;
import pe.edu.upeu.bomerp.inventario.dto.MovimientoKardexDto;
import pe.edu.upeu.bomerp.inventario.service.InventarioService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
@Tag(name = "Inventario y FEFO", description = "Gestión de lotes de producción, caducidad FEFO y kardex de almacén")
public class InventarioController {

    private final InventarioService inventarioService;

    @PostMapping("/lotes")
    @Operation(summary = "Registrar nuevo lote de producción con fecha de caducidad")
    public ResponseEntity<LoteDto> registrarLote(@Valid @RequestBody LoteCreateDto dto) {
        LoteDto creado = inventarioService.registrarLote(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/lotes")
    @Operation(summary = "Listar lotes de producción, opcionalmente filtrados por producto")
    public ResponseEntity<List<LoteDto>> listarLotes(@RequestParam(required = false) Long productoId) {
        return ResponseEntity.ok(inventarioService.listarLotes(productoId));
    }

    @GetMapping("/lotes/alertas-vencimiento")
    @Operation(summary = "Listar lotes en riesgo de caducidad (próximos a vencer en N días)")
    public ResponseEntity<List<LoteDto>> listarAlertasVencimiento(@RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(inventarioService.listarLotesProximosAVencer(dias));
    }

    @PostMapping("/despacho-fefo")
    @Operation(summary = "Ejecutar descuento automático de stock bajo regla FEFO (First Expired, First Out)")
    public ResponseEntity<DescuentoFefoResponse> ejecutarDespachoFEFO(@Valid @RequestBody DescuentoFefoRequest request) {
        return ResponseEntity.ok(inventarioService.descontarStockFEFO(request));
    }

    @GetMapping("/kardex")
    @Operation(summary = "Consultar el historial y trazabilidad del Kardex de movimientos de almacén")
    public ResponseEntity<List<MovimientoKardexDto>> listarKardex(@RequestParam(required = false) Long productoId) {
        return ResponseEntity.ok(inventarioService.listarKardex(productoId));
    }

    @PostMapping("/evaluar-vencimientos")
    @Operation(summary = "Actualizar estados de lotes según la fecha actual del sistema")
    public ResponseEntity<Void> evaluarVencimientos() {
        inventarioService.evaluarEstadosVencimiento();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reporte-semaforo")
    @Operation(summary = "Reporte analítico de semáforo de caducidad FEFO y valorización de inventario")
    public ResponseEntity<pe.edu.upeu.bomerp.inventario.dto.ReporteSemaforoInventarioResponse> obtenerReporteSemaforo() {
        return ResponseEntity.ok(inventarioService.obtenerReporteSemaforoInventario());
    }
}
