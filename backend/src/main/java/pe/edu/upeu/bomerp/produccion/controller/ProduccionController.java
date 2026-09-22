package pe.edu.upeu.bomerp.produccion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.produccion.dto.*;
import pe.edu.upeu.bomerp.produccion.entity.EstadoOrdenProduccion;
import pe.edu.upeu.bomerp.produccion.service.ProduccionService;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Producción - Recetas BOM y Lotes", description = "Planificación de órdenes, consumo de insumos y generación de lotes FEFO (Isaí Armuto)")
@RestController
@RequestMapping("/api/v1/produccion")
@RequiredArgsConstructor
public class ProduccionController {

    private final ProduccionService produccionService;

    @Operation(summary = "Crear nueva orden de producción con lista de materiales (BOM)")
    @PostMapping("/ordenes")
    public ResponseEntity<OrdenProduccionResponse> crearOrden(@Valid @RequestBody OrdenProduccionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produccionService.crearOrden(request));
    }

    @Operation(summary = "Iniciar orden de producción en planta")
    @PutMapping("/ordenes/{id}/iniciar")
    public ResponseEntity<OrdenProduccionResponse> iniciarOrden(@PathVariable Long id) {
        return ResponseEntity.ok(produccionService.iniciarOrden(id));
    }

    @Operation(summary = "Completar orden, dar alta a stock de producto y generar lote con fecha de vencimiento")
    @PutMapping("/ordenes/{id}/completar")
    public ResponseEntity<OrdenProduccionResponse> completarOrden(@PathVariable Long id, @Valid @RequestBody CompletarOrdenRequest request) {
        return ResponseEntity.ok(produccionService.completarOrden(id, request));
    }

    @Operation(summary = "Cancelar orden de producción pendiente")
    @PostMapping("/ordenes/{id}/cancelar")
    public ResponseEntity<OrdenProduccionResponse> cancelarOrden(@PathVariable Long id) {
        return ResponseEntity.ok(produccionService.cancelarOrden(id));
    }

    @Operation(summary = "Generar reporte agregado de eficiencia de producción")
    @GetMapping("/resumen")
    public ResponseEntity<ProduccionReporte> reporte(
            @RequestParam(required = false) EstadoOrdenProduccion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(produccionService.reporte(estado, desde, hasta));
    }

    @Operation(summary = "Consultar orden de producción por ID con detalle de insumos consumidos")
    @GetMapping("/ordenes/{id}")
    public ResponseEntity<OrdenProduccionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(produccionService.obtener(id));
    }

    @Operation(summary = "Listar todas las órdenes de producción")
    @GetMapping("/ordenes")
    public ResponseEntity<List<OrdenProduccionResponse>> listarOrdenes() {
        return ResponseEntity.ok(produccionService.listarOrdenes());
    }

    @Operation(summary = "Consultar lotes disponibles de un producto ordenados por vencimiento más próximo (Criterio FEFO)")
    @GetMapping("/lotes/producto/{productoId}/fefo")
    public ResponseEntity<List<LoteProductoResponse>> listarLotesDisponiblesFEFO(@PathVariable Long productoId) {
        return ResponseEntity.ok(produccionService.listarLotesDisponiblesFEFO(productoId));
    }
}
