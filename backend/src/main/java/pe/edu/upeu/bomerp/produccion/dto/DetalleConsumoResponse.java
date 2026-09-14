package pe.edu.upeu.bomerp.produccion.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleConsumoResponse {
    private Long id;
    private Long insumoId;
    private String nombreInsumo;
    private BigDecimal cantidadRequerida;
    private BigDecimal cantidadConsumida;
    private String unidadMedida;
}
