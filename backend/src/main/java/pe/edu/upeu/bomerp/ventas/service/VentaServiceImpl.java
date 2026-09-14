package pe.edu.upeu.bomerp.ventas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.catalogo.producto.dto.ProductoResponse;
import pe.edu.upeu.bomerp.catalogo.producto.service.ProductoService;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.ventas.dto.*;
import pe.edu.upeu.bomerp.ventas.entity.DetalleVenta;
import pe.edu.upeu.bomerp.ventas.entity.EstadoVenta;
import pe.edu.upeu.bomerp.ventas.entity.Venta;
import pe.edu.upeu.bomerp.ventas.repository.VentaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoService productoService;

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponse> buscar(EstadoVenta estado, LocalDateTime desde, LocalDateTime hasta,
                                      String ordenarPor, String direccion) {
        Sort.Direction dir = "ASC".equalsIgnoreCase(direccion) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String campoOrden = (ordenarPor != null && !ordenarPor.isBlank()) ? ordenarPor : "fecha";
        Sort sort = Sort.by(dir, campoOrden);

        return ventaRepository.buscar(estado, desde, hasta, sort).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponse obtener(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));
        return toResponse(venta);
    }

    @Override
    @Transactional
    public VentaResponse crear(VentaRequest request) {
        String ticket = "TKT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + (int) (Math.random() * 900 + 100);

        Venta venta = Venta.builder()
                .numeroTicket(ticket)
                .fecha(LocalDateTime.now())
                .estado(EstadoVenta.REGISTRADA)
                .metodoPago(request.getMetodoPago())
                .clienteNombre(request.getClienteNombre() != null ? request.getClienteNombre() : "Cliente General")
                .detalles(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVentaRequest item : request.getDetalles()) {
            // Verificación y obtención de datos del producto
            ProductoResponse prod = productoService.obtener(item.getProductoId());

            // Descuento atómico de stock (si no alcanza, lanza BusinessConflictException y revierte todo)
            productoService.descontarStock(item.getProductoId(), item.getCantidad());

            BigDecimal subtotalItem = prod.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));

            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .productoId(prod.getId())
                    .loteId(item.getLoteId())
                    .nombreProducto(prod.getNombre())
                    .precioUnitario(prod.getPrecio())
                    .cantidad(item.getCantidad())
                    .subtotal(subtotalItem)
                    .build();

            venta.getDetalles().add(detalle);
            total = total.add(subtotalItem);
        }

        BigDecimal igv = total.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = total.subtract(igv);

        venta.setSubtotal(subtotal);
        venta.setIgv(igv);
        venta.setTotal(total);

        Venta guardada = ventaRepository.save(venta);
        return toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaReporte reporte(EstadoVenta estado, LocalDateTime desde, LocalDateTime hasta) {
        VentaAgregado agregado = ventaRepository.agregados(estado, desde, hasta);
        Sort sort = Sort.by(Sort.Direction.DESC, "fecha");
        List<VentaResumen> resumenes = ventaRepository.buscarResumen(estado, desde, hasta, sort);
        return new VentaReporte(agregado, resumenes);
    }

    private VentaResponse toResponse(Venta v) {
        List<DetalleVentaResponse> detalles = v.getDetalles() != null
                ? v.getDetalles().stream().map(d -> DetalleVentaResponse.builder()
                .id(d.getId())
                .productoId(d.getProductoId())
                .loteId(d.getLoteId())
                .nombreProducto(d.getNombreProducto())
                .precioUnitario(d.getPrecioUnitario())
                .cantidad(d.getCantidad())
                .subtotal(d.getSubtotal())
                .build()).toList()
                : List.of();

        return VentaResponse.builder()
                .id(v.getId())
                .numeroTicket(v.getNumeroTicket())
                .fecha(v.getFecha())
                .estado(v.getEstado())
                .metodoPago(v.getMetodoPago())
                .subtotal(v.getSubtotal())
                .igv(v.getIgv())
                .total(v.getTotal())
                .clienteNombre(v.getClienteNombre())
                .detalles(detalles)
                .build();
    }
}
