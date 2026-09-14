package pe.edu.upeu.bomerp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI agroPeruvianOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ERP Agro Peruvian Foods API")
                        .version("v1")
                        .description("Contrato REST modular para la gestión de Caja, Ventas POS, Producción BOM y Compras.")
                        .contact(new Contact().name("Los Hijos del Backend")));
    }
}
