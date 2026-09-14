package pe.edu.upeu.bomerp.caja.dto;

import lombok.*;
import pe.edu.upeu.bomerp.caja.entity.MetodoPagoCaja;
import pe.edu.upeu.bomerp.caja.entity.TipoMovimientoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCajaResponse {
    private Long id;
    private TipoMovimientoCaja tipo;
    private BigDecimal monto;
    private String concepto;
    private MetodoPagoCaja metodoPago;
    private String referencia;
    private LocalDateTime fecha;
}
