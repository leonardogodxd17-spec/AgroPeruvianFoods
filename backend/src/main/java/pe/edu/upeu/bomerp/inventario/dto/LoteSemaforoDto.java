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
public class LoteSemaforoDto {
    private Long loteId;
    private String codigoLote;
    private Long productoId;
    private String productoNombre;
    private LocalDate fechaFabricacion;
    private LocalDate fechaVencimiento;
    private Long diasRestantes;
    /**
     * "CRITICO" (< 30 días), "PREVENTIVO" (30 a 60 días), "OPTIMO" (> 60 días), "VENCIDO" (<= 0 días)
     */
    private String estadoSemaforo;
    private BigDecimal stockActual;
    private BigDecimal costoUnitario;
    private BigDecimal valorizacionTotal;
}
