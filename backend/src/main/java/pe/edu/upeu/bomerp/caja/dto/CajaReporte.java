package pe.edu.upeu.bomerp.caja.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajaReporte {
    private Long totalSesiones;
    private Long sesionesAbiertas;
    private Long sesionesCerradas;
    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;
    private BigDecimal saldoNetoEfectivo;
    private BigDecimal diferenciasAcumuladas;
    private List<SesionCajaResponse> sesiones;
}
