package pe.edu.upeu.bomerp.caja.dto;

import lombok.*;
import pe.edu.upeu.bomerp.caja.entity.EstadoSesionCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionCajaResponse {
    private Long id;
    private String cajeroNombre;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private BigDecimal montoApertura;
    private BigDecimal montoCierre;
    private BigDecimal saldoTeorico;
    private BigDecimal saldoReal;
    private BigDecimal diferencia;
    private EstadoSesionCaja estado;
    private String observaciones;
    private List<MovimientoCajaResponse> movimientos;
}
