package pe.edu.upeu.bomerp.produccion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletarOrdenRequest {

    @NotNull(message = "La cantidad producida real es obligatoria")
    @Positive(message = "La cantidad producida debe ser mayor a 0")
    private Integer cantidadProducida;

    @NotNull(message = "La fecha de vencimiento es obligatoria para el lote")
    private LocalDate fechaVencimiento;
}
