package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
          .info(new Info().title("SafeTemporaryDbAccess API").version("1.0"))
          .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
          .components(new Components()
            .addSecuritySchemes(SECURITY_SCHEME_NAME,
              new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
            ));
    }
}
