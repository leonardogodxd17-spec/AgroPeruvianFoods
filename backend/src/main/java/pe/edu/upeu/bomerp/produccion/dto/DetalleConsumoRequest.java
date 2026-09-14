package pe.edu.upeu.bomerp.produccion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleConsumoRequest {

    @NotNull(message = "El ID del insumo es obligatorio")
    private Long insumoId;

    @NotBlank(message = "El nombre del insumo es obligatorio")
    private String nombreInsumo;

    @NotNull(message = "La cantidad requerida es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad requerida debe ser mayor a 0")
    private BigDecimal cantidadRequerida;

    @NotNull(message = "La cantidad consumida es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad consumida debe ser mayor a 0")
    private BigDecimal cantidadConsumida;

    @NotBlank(message = "La unidad de medida es obligatoria (kg, L, unid, etc.)")
    private String unidadMedida;
}
