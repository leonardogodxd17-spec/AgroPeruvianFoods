package pe.edu.upeu.bomerp.inventario.dto;

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
public class LoteDescontadoItem {
    private Long loteId;
    private String codigoLote;
    private LocalDate fechaVencimiento;
    private BigDecimal cantidadDescontada;
    private BigDecimal stockRestanteLote;
    private boolean loteAgotado;
}
