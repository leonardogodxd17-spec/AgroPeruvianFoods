package pe.edu.upeu.bomerp.seguridad.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.bomerp.seguridad.auth.dto.LoginRequest;
import pe.edu.upeu.bomerp.seguridad.auth.dto.LoginResponse;
import pe.edu.upeu.bomerp.seguridad.auth.dto.PerfilResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Tag(name = "Autenticación y Seguridad", description = "Inicio de sesión directo en Swagger, obtención de token JWT Keycloak y consulta de perfil")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:38080/realms/bomerp}")
    private String issuerUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Operation(
            summary = "Iniciar sesión y obtener Token JWT Bearer",
            description = "Introduce tu usuario (ej. 'admin', 'vendedor', 'cajero') y contraseña ('Bomerp2026!'). " +
                    "Al ejecutar, obtendrás el 'accessToken' directamente en la respuesta para copiarlo y pegarlo en el botón verde 'Authorize' de Swagger."
    )
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String tokenUrl = issuerUri + "/protocol/openid-connect/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", "bomerp-frontend");
        params.add("username", request.getUsername().trim());
        params.add("password", request.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("access_token")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se recibió token de acceso de Keycloak");
            }

            String accessToken = (String) body.get("access_token");
            long expiresIn = body.containsKey("expires_in") ? ((Number) body.get("expires_in")).longValue() : 3600L;
            String tokenType = (String) body.getOrDefault("token_type", "Bearer");

            List<String> roles = extraerRolesDeToken(accessToken);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .tokenType(tokenType)
                    .expiresIn(expiresIn)
                    .username(request.getUsername().trim())
                    .roles(roles)
                    .build();

        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED || ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas en Keycloak. Verifica usuario y contraseña.");
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error de comunicación con Keycloak: " + ex.getMessage());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al autenticar: " + ex.getMessage());
        }
    }

    @Operation(summary = "Consultar perfil y roles del usuario autenticado actual")
    @GetMapping("/perfil")
    public PerfilResponse perfil(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Petición no autenticada con token Bearer");
        }

        String username = jwt.getClaimAsString("preferred_username");
        if (username == null) {
            username = jwt.getSubject();
        }

        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = username + "@bomerp.pe";
        }

        List<String> roles = Collections.emptyList();
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> col) {
            roles = col.stream().map(Object::toString).toList();
        }

        return PerfilResponse.builder()
                .id(jwt.getSubject())
                .username(username)
                .email(email)
                .roles(roles)
                .build();
    }

    @Operation(summary = "Listado de usuarios de prueba preconfigurados en Keycloak")
    @GetMapping("/usuarios-demo")
    public List<Map<String, Object>> usuariosDemo() {
        return List.of(
                Map.of(
                        "username", "admin",
                        "password", "Bomerp2026!",
                        "roles", List.of("ADMIN", "ADMIN_FINANZAS", "SUPERVISOR", "VENDEDOR", "CAJERO"),
                        "descripcion", "Administrador Total con acceso a todos los módulos (Ventas, Finanzas, Producción, Compras, Catálogo)"
                ),
                Map.of(
                        "username", "vendedor",
                        "password", "Bomerp2026!",
                        "roles", List.of("VENDEDOR", "CAJERO", "SUPERVISOR"),
                        "descripcion", "Vendedor POS para emisión de ventas, tickets y pedidos delivery"
                ),
                Map.of(
                        "username", "cajero",
                        "password", "Bomerp2026!",
                        "roles", List.of("CAJERO"),
                        "descripcion", "Cajero operativo para arqueo de caja y cobros"
                )
        );
    }

    private List<String> extraerRolesDeToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return List.of();
            byte[] decoded = java.util.Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<?, ?> claims = mapper.readValue(payloadJson, Map.class);
            Object realmAccessObj = claims.get("realm_access");
            if (realmAccessObj instanceof Map<?, ?> realmAccess) {
                Object rolesObj = realmAccess.get("roles");
                if (rolesObj instanceof Collection<?> rolesColl) {
                    List<String> lista = new ArrayList<>();
                    for (Object r : rolesColl) {
                        lista.add(r.toString());
                    }
                    return lista;
                }
            }
        } catch (Exception ignored) {
        }
        return List.of();
    }
}
