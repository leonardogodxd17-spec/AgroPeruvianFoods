package pe.edu.upeu.bomerp.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoFefoResponse {
    private Long productoId;
    private String productoNombre;
    private BigDecimal cantidadTotalSolicitada;
    private BigDecimal cantidadTotalDescontada;
    private BigDecimal stockTotalRestante;
    private List<LoteDescontadoItem> lotesAfectados;
}
