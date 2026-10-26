package pe.edu.upeu.bomerp.inventario.service;

import pe.edu.upeu.bomerp.inventario.dto.DescuentoFefoRequest;
import pe.edu.upeu.bomerp.inventario.dto.DescuentoFefoResponse;
import pe.edu.upeu.bomerp.inventario.dto.LoteCreateDto;
import pe.edu.upeu.bomerp.inventario.dto.LoteDto;
import pe.edu.upeu.bomerp.inventario.dto.MovimientoKardexDto;
import pe.edu.upeu.bomerp.inventario.dto.ReporteSemaforoInventarioResponse;

import java.util.List;

public interface InventarioService {

    LoteDto registrarLote(LoteCreateDto dto);

    /**
     * Regla FEFO (First Expired, First Out):
     * Descuenta stock de un producto consumiendo automáticamente primero los lotes
     * cuya fecha de caducidad esté más próxima.
     */
    DescuentoFefoResponse descontarStockFEFO(DescuentoFefoRequest request);

    List<LoteDto> listarLotes(Long productoId);

    List<LoteDto> listarLotesProximosAVencer(int diasUmbral);

    List<MovimientoKardexDto> listarKardex(Long productoId);

    void evaluarEstadosVencimiento();

    ReporteSemaforoInventarioResponse obtenerReporteSemaforoInventario();
}
