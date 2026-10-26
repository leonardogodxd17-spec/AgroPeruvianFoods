package pe.edu.upeu.bomerp.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.bomerp.inventario.entity.EstadoLote;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteDto {
    private Long id;
    private String codigoLote;
    private Long productoId;
    private String productoNombre;
    private LocalDate fechaFabricacion;
    private LocalDate fechaVencimiento;
    private BigDecimal stockInicial;
    private BigDecimal stockActual;
    private BigDecimal costoUnitario;
    private EstadoLote estado;
    private String observaciones;
    private Long diasParaVencer;
}
