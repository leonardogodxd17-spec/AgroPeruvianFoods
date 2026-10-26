package pe.edu.upeu.bomerp.finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.bomerp.finanzas.entity.TipoDenominacion;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DenominacionMonedaResponse {
    private Long id;
    private BigDecimal valorFacial;
    private TipoDenominacion tipo;
    private Integer orden;
    private Boolean activo;
}
