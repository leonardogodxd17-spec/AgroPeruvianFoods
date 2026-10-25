package pe.edu.upeu.bomerp.ventas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.bomerp.ventas.dto.DniConsultaResponse;
import pe.edu.upeu.bomerp.ventas.service.ConsultaDniService;

import static org.junit.jupiter.api.Assertions.*;

class ConsultaDniServiceTest {

    private ConsultaDniService consultaDniService;

    @BeforeEach
    void setUp() {
        consultaDniService = new ConsultaDniService();
    }

    @Test
    @DisplayName("Debe retornar respuesta estructurada con nombre completo para DNI válido de 8 dígitos")
    void buscarPorDni_dniValido_retornaDatos() {
        String dni = "72345678";
        DniConsultaResponse respuesta = consultaDniService.buscarPorDni(dni);

        assertNotNull(respuesta);
        assertEquals(dni, respuesta.getDni());
        assertNotNull(respuesta.getNombres());
        assertFalse(respuesta.getNombreCompleto().isBlank());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el DNI no tiene 8 dígitos numéricos")
    void buscarPorDni_dniInvalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> consultaDniService.buscarPorDni("123"));
        assertThrows(IllegalArgumentException.class, () -> consultaDniService.buscarPorDni("ABCDEFGH"));
        assertThrows(IllegalArgumentException.class, () -> consultaDniService.buscarPorDni(""));
        assertThrows(IllegalArgumentException.class, () -> consultaDniService.buscarPorDni(null));
    }
}
