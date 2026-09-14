package pe.edu.upeu.bomerp.produccion.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteProductoResponse {
    private Long id;
    private String codigoLote;
    private Long productoId;
    private String nombreProducto;
    private Integer stockDisponible;
    private LocalDate fechaProduccion;
    private LocalDate fechaVencimiento;
}
