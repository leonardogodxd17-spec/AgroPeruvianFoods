package pe.edu.upeu.bomerp.compras.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorResponse {
    private Long id;
    private String ruc;
    private String razonSocial;
    private String telefono;
    private String email;
    private String direccion;
}
