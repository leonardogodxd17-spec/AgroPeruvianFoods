package pe.edu.upeu.bomerp.inventario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteCreateDto {

    @NotBlank(message = "El código de lote es obligatorio")
    @Size(max = 50, message = "El código de lote no puede superar 50 caracteres")
    private String codigoLote;

    @NotNull(message = "El ID de producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La fecha de fabricación es obligatoria")
    private LocalDate fechaFabricacion;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

    @NotNull(message = "El stock inicial es obligatorio")
    @DecimalMin(value = "0.01", message = "El stock inicial debe ser mayor a 0")
    private BigDecimal stockInicial;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo unitario debe ser mayor a 0")
    private BigDecimal costoUnitario;

    @Size(max = 255, message = "Las observaciones no pueden superar 255 caracteres")
    private String observaciones;
}
