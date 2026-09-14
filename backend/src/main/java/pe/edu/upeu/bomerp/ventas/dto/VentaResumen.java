package pe.edu.upeu.bomerp.ventas.dto;

import lombok.Getter;
import pe.edu.upeu.bomerp.ventas.entity.EstadoVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class VentaResumen {
    private final Long id;
    private final String numeroTicket;
    private final LocalDateTime fecha;
    private final String estado;
    private final BigDecimal total;
    private final long cantidadDetalles;

    public VentaResumen(Long id, String numeroTicket, LocalDateTime fecha, EstadoVenta estado, BigDecimal total, long cantidadDetalles) {
        this.id = id;
        this.numeroTicket = numeroTicket;
        this.fecha = fecha;
        this.estado = estado.name();
        this.total = total;
        this.cantidadDetalles = cantidadDetalles;
    }
}
