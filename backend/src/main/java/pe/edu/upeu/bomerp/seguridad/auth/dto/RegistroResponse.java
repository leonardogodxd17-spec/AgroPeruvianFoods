package pe.edu.upeu.bomerp.seguridad.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistroResponse {
    private Long id;
    private String email;
}
