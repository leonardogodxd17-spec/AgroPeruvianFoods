package pe.edu.upeu.bomerp.seguridad.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.seguridad.auth.dto.LoginRequest;
import pe.edu.upeu.bomerp.seguridad.auth.dto.LoginResponse;
import pe.edu.upeu.bomerp.seguridad.auth.dto.PerfilResponse;
import pe.edu.upeu.bomerp.seguridad.auth.dto.RegistroRequest;
import pe.edu.upeu.bomerp.seguridad.auth.dto.RegistroResponse;
import pe.edu.upeu.bomerp.seguridad.auth.service.AuthService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse registro(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @GetMapping("/perfil")
    public PerfilResponse perfil(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return PerfilResponse.builder().build();
        }
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        Object rolesObj = realmAccess == null ? null : realmAccess.get("roles");
        List<String> roles = List.of();
        if (rolesObj instanceof Collection<?> lista) {
            roles = lista.stream().map(Object::toString).toList();
        }

        return PerfilResponse.builder()
                .id(jwt.getSubject())
                .email(jwt.getClaimAsString("email"))
                .roles(roles)
                .build();
    }
}
