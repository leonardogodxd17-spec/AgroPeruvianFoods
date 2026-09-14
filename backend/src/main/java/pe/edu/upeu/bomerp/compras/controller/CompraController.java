package pe.edu.upeu.bomerp.compras.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.compras.dto.*;
import pe.edu.upeu.bomerp.compras.service.CompraService;

import java.util.List;

@Tag(name = "Compras - Insumos y Proveedores", description = "Adquisición de materias primas, material de empaque y cuentas por pagar (Elisban Huaylla)")
@RestController
@RequestMapping("/api/v1/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @Operation(summary = "Registrar un nuevo proveedor")
    @PostMapping("/proveedores")
    public ResponseEntity<ProveedorResponse> crearProveedor(@Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.crearProveedor(request));
    }

    @Operation(summary = "Listar todos los proveedores registrados")
    @GetMapping("/proveedores")
    public ResponseEntity<List<ProveedorResponse>> listarProveedores() {
        return ResponseEntity.ok(compraService.listarProveedores());
    }

    @Operation(summary = "Registrar una compra con su colección de insumos adquiridos")
    @PostMapping
    public ResponseEntity<CompraResponse> registrarCompra(@Valid @RequestBody CompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.registrarCompra(request));
    }

    @Operation(summary = "Amortizar pago o liquidar saldo pendiente de una compra")
    @PutMapping("/{id}/amortizar")
    public ResponseEntity<CompraResponse> amortizarPago(@PathVariable Long id, @Valid @RequestBody AmortizarPagoRequest request) {
        return ResponseEntity.ok(compraService.amortizarPago(id, request));
    }

    @Operation(summary = "Consultar compra por ID con el detalle de insumos adquiridos")
    @GetMapping("/{id}")
    public ResponseEntity<CompraResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.obtener(id));
    }

    @Operation(summary = "Listar todas las compras registradas")
    @GetMapping
    public ResponseEntity<List<CompraResponse>> listarCompras() {
        return ResponseEntity.ok(compraService.listarCompras());
    }
}
