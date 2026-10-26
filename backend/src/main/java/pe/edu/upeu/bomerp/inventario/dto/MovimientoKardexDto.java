package pe.edu.upeu.bomerp.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.bomerp.inventario.entity.TipoMovimientoKardex;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoKardexDto {
    private Long id;
    private TipoMovimientoKardex tipo;
    private Long loteId;
    private String codigoLote;
    private Long productoId;
    private String productoNombre;
    private BigDecimal cantidad;
    private BigDecimal saldoStockResultante;
    private String motivo;
    private String documentoReferencia;
    private LocalDateTime fechaHora;
}
