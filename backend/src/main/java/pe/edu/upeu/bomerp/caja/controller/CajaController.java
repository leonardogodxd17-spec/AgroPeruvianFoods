package pe.edu.upeu.bomerp.caja.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.caja.dto.*;
import pe.edu.upeu.bomerp.caja.service.CajaService;

import java.util.List;

@Tag(name = "Finanzas - Gestión de Caja", description = "Control de turnos, aperturas, ingresos, egresos y arqueo de caja (Brandon Ccalla)")
@RestController
@RequestMapping("/api/v1/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    @Operation(summary = "Abrir un turno de caja")
    @PostMapping("/apertura")
    public ResponseEntity<SesionCajaResponse> abrirCaja(@Valid @RequestBody AperturaCajaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.abrirCaja(request));
    }

    @Operation(summary = "Registrar un movimiento de ingreso o egreso en la caja activa")
    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoCajaResponse> registrarMovimiento(@Valid @RequestBody MovimientoCajaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.registrarMovimiento(request));
    }

    @Operation(summary = "Cerrar sesión de caja y asentar arqueo (saldo real vs saldo teórico)")
    @PostMapping("/{id}/cierre")
    public ResponseEntity<SesionCajaResponse> cerrarCaja(@PathVariable Long id, @Valid @RequestBody CierreCajaRequest request) {
        return ResponseEntity.ok(cajaService.cerrarCaja(id, request));
    }

    @Operation(summary = "Consultar la sesión de caja actualmente abierta")
    @GetMapping("/activa")
    public ResponseEntity<SesionCajaResponse> obtenerSesionActiva() {
        return ResponseEntity.ok(cajaService.obtenerSesionActiva());
    }

    @Operation(summary = "Listar el historial de todas las sesiones de caja")
    @GetMapping("/historial")
    public ResponseEntity<List<SesionCajaResponse>> listarHistorial() {
        return ResponseEntity.ok(cajaService.listarHistorial());
    }

    @Operation(summary = "Generar reporte consolidado de tesorería y arqueos")
    @GetMapping("/resumen")
    public ResponseEntity<CajaReporte> reporteResumen() {
        return ResponseEntity.ok(cajaService.reporteResumen());
    }
}
