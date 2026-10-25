package pe.edu.upeu.bomerp.ventas.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.ventas.dto.DniConsultaResponse;

@Slf4j
@Service
public class ConsultaDniService {

    @Value("${apisperu.base-url:https://dniruc.apisperu.com/api/v1}")
    private String baseUrl;

    @Value("${apisperu.token:}")
    private String token;

    private final RestClient restClient;

    public ConsultaDniService() {
        this.restClient = RestClient.builder().build();
    }

    public DniConsultaResponse buscarPorDni(String dni) {
        if (dni == null || !dni.matches("^[0-9]{8}$")) {
            throw new IllegalArgumentException("El DNI debe contener exactamente 8 dígitos numéricos.");
        }

        // Si tenemos un token válido configurado, invocamos el endpoint de APIsPERU
        if (token != null && !token.isBlank() && !token.startsWith("pega_")) {
            try {
                DniConsultaResponse resp = restClient.get()
                        .uri(baseUrl + "/dni/{numero}?token={token}", dni, token)
                        .retrieve()
                        .body(DniConsultaResponse.class);

                if (resp != null && resp.getDni() != null && !resp.getDni().isBlank()) {
                    return resp;
                }
                throw new ResourceNotFoundException("No se encontraron datos en RENIEC para el DNI: " + dni);
            } catch (ResourceNotFoundException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Error consultando APIsPERU para DNI {}: {}. Activando respuesta de respaldo.", dni, e.getMessage());
            }
        }

        // Modo respaldo (Fallback) para pruebas locales, desarrollo o cuando no hay conectividad
        return DniConsultaResponse.builder()
                .dni(dni)
                .nombres("CLIENTE REGISTRADO")
                .apellidoPaterno("AGRO")
                .apellidoMaterno("PERUVIAN")
                .codVerifica("0")
                .build();
    }
}
