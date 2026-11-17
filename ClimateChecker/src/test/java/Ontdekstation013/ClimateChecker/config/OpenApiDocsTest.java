package Ontdekstation013.ClimateChecker.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Draait met het testprofiel en dus met application-test.properties, waarin springdoc aan staat.
 * De main-properties worden expliciet geimporteerd (zie HealthEndpointTest).
 */
@SpringBootTest(properties = {
        "spring.config.import=file:src/main/resources/application.properties",
        "spring.datasource.url=jdbc:h2:mem:docsdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "application.root-user.first-name=Admin",
        "application.root-user.last-name=User",
        "application.root-user.email=admin@example.com",
        "application.root-user.password=password"
})
@AutoConfigureMockMvc
class OpenApiDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsArePublicAndListEveryController() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value(org.hamcrest.Matchers.startsWith("3.")))
                .andExpect(jsonPath("$.info.title").value("ClimateChecker API"))
                .andExpect(jsonPath("$.paths['/api/authentication/login']").exists())
                .andExpect(jsonPath("$.paths['/api/Meetstation/stations']").exists())
                .andExpect(jsonPath("$.paths['/api/measurement/history']").exists())
                .andExpect(jsonPath("$.paths['/api/neighbourhood/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/users']").exists())
                .andExpect(jsonPath("$.paths['/api/workshops']").exists())
                .andExpect(jsonPath("$.paths['/api/my-account']").exists());
    }

    @Test
    void specDescribesJwtBearerSchemeAndMarksSecuredEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.paths['/api/users'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/authentication/login'].post.security").doesNotExist());
    }

    @Test
    void swaggerUiIsReachableInTestProfile() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void securedEndpointsStillRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().is4xxClientError());
    }
}
