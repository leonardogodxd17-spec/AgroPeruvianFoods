package pe.edu.upeu.bomerp.caja.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CierreCajaRequest {

    @NotNull(message = "El saldo físico reportado es obligatorio")
    @PositiveOrZero(message = "El saldo físico debe ser mayor o igual a 0")
    private BigDecimal saldoReal;

    private String observaciones;
}
