package pe.edu.upeu.bomerp.produccion.service;

import pe.edu.upeu.bomerp.produccion.dto.*;
import pe.edu.upeu.bomerp.produccion.entity.EstadoOrdenProduccion;

import java.time.LocalDateTime;
import java.util.List;

public interface ProduccionService {
    OrdenProduccionResponse crearOrden(OrdenProduccionRequest request);
    OrdenProduccionResponse iniciarOrden(Long id);
    OrdenProduccionResponse completarOrden(Long id, CompletarOrdenRequest request);
    OrdenProduccionResponse cancelarOrden(Long id);
    OrdenProduccionResponse obtener(Long id);
    List<OrdenProduccionResponse> listarOrdenes();
    ProduccionReporte reporte(EstadoOrdenProduccion estado, LocalDateTime desde, LocalDateTime hasta);
    List<LoteProductoResponse> listarLotesPorProducto(Long productoId);
    List<LoteProductoResponse> listarLotesDisponiblesFEFO(Long productoId);
    void descontarStockLoteFEFO(Long productoId, Integer cantidad);
}
