package pe.edu.upeu.bomerp.caja.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AperturaCajaRequest {

    @NotBlank(message = "El nombre del cajero es obligatorio")
    private String cajeroNombre;

    @NotNull(message = "El monto de apertura es obligatorio")
    @PositiveOrZero(message = "El monto de apertura debe ser mayor o igual a cero")
    private BigDecimal montoApertura;
}
