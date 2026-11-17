package Ontdekstation013.ClimateChecker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beschrijving van de API voor springdoc. Authenticatie verloopt uitsluitend via de
 * header {@code Authorization: Bearer <jwt>} (zie JwtAuthenticationFilter), er worden
 * geen cookies gebruikt. Beveiligde endpoints verwijzen met
 * {@code @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)} naar dit scheme.
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI climateCheckerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ClimateChecker API")
                        .version("1.0")
                        .description("""
                                REST API van ClimateChecker: meetstations, metingen en wijken van \
                                de klimaatkaart, plus accountbeheer en workshops.

                                Inloggen: roep `POST /api/authentication/login` aan (en daarna \
                                `POST /api/authentication/verify` met de code uit de e-mail). \
                                Plak het ontvangen token via de knop "Authorize" om beveiligde \
                                endpoints uit te proberen."""))
                .components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT uit het login- of verify-antwoord, meegestuurd als `Authorization: Bearer <token>`.")));
    }
}
