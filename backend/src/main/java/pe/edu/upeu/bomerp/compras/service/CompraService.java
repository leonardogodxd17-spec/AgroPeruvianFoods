package pe.edu.upeu.bomerp.compras.service;

import pe.edu.upeu.bomerp.compras.dto.*;

import java.util.List;

public interface CompraService {
    ProveedorResponse crearProveedor(ProveedorRequest request);
    List<ProveedorResponse> listarProveedores();
    CompraResponse registrarCompra(CompraRequest request);
    CompraResponse amortizarPago(Long compraId, AmortizarPagoRequest request);
    CompraResponse obtener(Long id);
    List<CompraResponse> listarCompras();
}
