package pe.edu.upeu.bomerp.caja.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.edu.upeu.bomerp.caja.entity.MetodoPagoCaja;
import pe.edu.upeu.bomerp.caja.entity.TipoMovimientoCaja;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCajaRequest {

    @NotNull(message = "El tipo de movimiento es obligatorio (INGRESO o EGRESO)")
    private TipoMovimientoCaja tipo;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotBlank(message = "El concepto del movimiento es obligatorio")
    private String concepto;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPagoCaja metodoPago;

    private String referencia;
}
