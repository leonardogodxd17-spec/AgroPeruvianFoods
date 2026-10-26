package pe.edu.upeu.bomerp.seguridad.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilResponse {

    @Schema(description = "Identificador único (Subject) del usuario en Keycloak")
    private String id;

    @Schema(description = "Nombre de usuario o login", example = "admin")
    private String username;

    @Schema(description = "Correo electrónico registrado", example = "admin@bomerp.com")
    private String email;

    @Schema(description = "Roles vigentes del usuario en el token JWT actual")
    private List<String> roles;
}
