package pe.edu.upeu.bomerp.seguridad.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Schema(description = "Nombre de usuario o correo (admin, vendedor, cajero)", example = "admin")
    @NotBlank(message = "El nombre de usuario o email es obligatorio")
    private String username;

    @Schema(description = "Contraseña de acceso en Keycloak", example = "Bomerp2026!")
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
