package pe.edu.upeu.bomerp.finanzas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.bomerp.finanzas.entity.TipoFinanciero;

@Getter
@Setter
@Builder
public class CategoriaFinancieraRequest {
    @NotBlank(message = "El nombre de la categoría financiera es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El tipo predeterminado (INGRESO/EGRESO) es obligatorio")
    private TipoFinanciero tipoDefault;

    public CategoriaFinancieraRequest() {
    }

    public CategoriaFinancieraRequest(String nombre, String descripcion, TipoFinanciero tipoDefault) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoDefault = tipoDefault;
    }
}
