package pe.edu.upeu.bomerp.ventas.service;

import pe.edu.upeu.bomerp.ventas.dto.VentaReporte;
import pe.edu.upeu.bomerp.ventas.dto.VentaRequest;
import pe.edu.upeu.bomerp.ventas.dto.VentaResponse;
import pe.edu.upeu.bomerp.ventas.entity.EstadoVenta;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {
    List<VentaResponse> buscar(EstadoVenta estado, LocalDateTime desde, LocalDateTime hasta, String ordenarPor, String direccion);
    VentaResponse obtener(Long id);
    VentaResponse crear(VentaRequest request);
    VentaResponse anular(Long id);
    VentaReporte reporte(EstadoVenta estado, LocalDateTime desde, LocalDateTime hasta);
}
