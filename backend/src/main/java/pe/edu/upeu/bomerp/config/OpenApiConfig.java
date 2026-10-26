package pe.edu.upeu.bomerp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    OpenAPI agroPeruvianOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ERP Agro Peruvian Foods API")
                        .version("v1")
                        .description("Contrato REST modular seguro para la gestión de Ventas POS, Finanzas, Inventario FEFO, Catálogo y Compras (S10b/S11/S12 - Keycloak OAuth2 & RBAC).")
                        .contact(new Contact().name("Los Hijos del Backend - Sección G2G3")))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
