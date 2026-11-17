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
 * Zonder dev/test-profiel (zoals productie) staat de documentatie uit en zijn de paden dicht.
 */
@SpringBootTest(properties = {
        "spring.config.import=file:src/main/resources/application.properties",
        "spring.datasource.url=jdbc:h2:mem:disableddb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "application.root-user.first-name=Admin",
        "application.root-user.last-name=User",
        "application.root-user.email=admin@example.com",
        "application.root-user.password=password",
        "spring.profiles.active=prodlike",
        "application.jwt.secret=cHJvZGxpa2Utc2VjcmV0LXRlc3Qtb25seS1hdC1sZWFzdC0zMi1ieXRlcy1sb25n"
})
@AutoConfigureMockMvc
class OpenApiDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsAreNotAvailableWhenDisabled() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().is4xxClientError());
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is4xxClientError());
    }
}
