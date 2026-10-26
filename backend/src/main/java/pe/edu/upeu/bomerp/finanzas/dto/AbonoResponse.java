package pe.edu.upeu.bomerp.finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.bomerp.finanzas.entity.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbonoResponse {
    private Long id;
    private Long cuentaPorCobrarId;
    private Long sesionCajaId;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private LocalDateTime fecha;
    private String nota;
}
