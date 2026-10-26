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
public class LoginResponse {

    @Schema(description = "Token de acceso Bearer JWT para copiar y pegar en Authorize de Swagger")
    private String accessToken;

    @Schema(description = "Tipo de esquema de autorización", example = "Bearer")
    private String tokenType;

    @Schema(description = "Tiempo de validez del token en segundos", example = "3600")
    private long expiresIn;

    @Schema(description = "Nombre de usuario autenticado", example = "admin")
    private String username;

    @Schema(description = "Roles asignados en el Realm Keycloak", example = "[\"ADMIN\", \"SUPERVISOR\", \"VENDEDOR\", \"CAJERO\"]")
    private List<String> roles;
}
