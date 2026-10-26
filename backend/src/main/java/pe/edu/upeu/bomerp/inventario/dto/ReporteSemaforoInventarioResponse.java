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
public class ReporteSemaforoInventarioResponse {
    private BigDecimal valorizacionTotalInventario;
    private Long totalLotes;
    private Long lotesCriticos;
    private Long lotesPreventivos;
    private Long lotesOptimos;
    private Long lotesVencidos;
    private List<LoteSemaforoDto> lotes;
}
