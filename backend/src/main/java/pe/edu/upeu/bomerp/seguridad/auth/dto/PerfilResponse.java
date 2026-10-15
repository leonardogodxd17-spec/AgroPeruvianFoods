package pe.edu.upeu.bomerp.seguridad.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PerfilResponse {
    private String id;
    private String email;
    private List<String> roles;
}
