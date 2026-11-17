package Ontdekstation013.ClimateChecker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
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

    /**
     * Old mixed-case spellings (for example {@code /api/Meetstation/Claim} or the typo
     * {@code Availibility}) stay reachable as aliases for existing clients but are left out of the
     * spec: the canonical paths are all lowercase, so any path with an uppercase letter outside a
     * {@code {parameter}} is a legacy alias.
     */
    @Bean
    public OpenApiCustomizer legacyPathAliasHider() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                openApi.getPaths().keySet().removeIf(OpenApiConfig::isLegacyAlias);
            }
        };
    }

    /**
     * A handler with several paths gets numbered operation ids ({@code addStation_1}, {@code ClaimStation_2}).
     * Once the aliases are hidden these numbers are meaningless, so strip them and start with a lowercase
     * letter, as long as the result stays unique. The ids become the request names in the generated Postman collection.
     */
    @Bean
    public OpenApiCustomizer cleanOperationIds() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            Set<String> used = new HashSet<>();
            openApi.getPaths().values().forEach(item -> item.readOperations().forEach(operation -> {
                String id = operation.getOperationId();
                if (id == null) {
                    return;
                }
                String candidate = id.replaceAll("_\\d+$", "");
                candidate = Character.toLowerCase(candidate.charAt(0)) + candidate.substring(1);
                if (used.add(candidate)) {
                    operation.setOperationId(candidate);
                } else {
                    used.add(id);
                }
            }));
        };
    }

    static boolean isLegacyAlias(String path) {
        return path.replaceAll("\\{[^}]*}", "").chars().anyMatch(Character::isUpperCase);
    }

    @Bean
    public OpenAPI climateCheckerOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server().url("http://localhost:8082").description("Lokale ontwikkelomgeving")))
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
