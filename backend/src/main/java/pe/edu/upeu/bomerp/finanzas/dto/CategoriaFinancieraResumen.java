package pe.edu.upeu.bomerp.finanzas.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.bomerp.finanzas.entity.TipoFinanciero;

@Getter
@Setter
@Builder
public class CategoriaFinancieraResumen {
    private Long id;
    private String nombre;
    private TipoFinanciero tipoDefault;

    public CategoriaFinancieraResumen() {
    }

    public CategoriaFinancieraResumen(Long id, String nombre, TipoFinanciero tipoDefault) {
        this.id = id;
        this.nombre = nombre;
        this.tipoDefault = tipoDefault;
    }
}
