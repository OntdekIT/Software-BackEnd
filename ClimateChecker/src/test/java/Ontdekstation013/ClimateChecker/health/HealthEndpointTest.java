package Ontdekstation013.ClimateChecker.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Draait zonder Docker tegen in-memory H2. src/test/resources/application.properties
 * schaduwt het main-bestand op de classpath, dus de echte main-properties (met de
 * management.*-config die we hier willen testen) worden expliciet geimporteerd en
 * de omgevingsafhankelijke waarden daarna weer overschreven.
 */
@SpringBootTest(properties = {
        "spring.config.import=file:src/main/resources/application.properties",
        "spring.datasource.url=jdbc:h2:mem:healthdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
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
public class HealthEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void healthIsPublicAndHidesDetails() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    public void livenessAndReadinessProbesArePublic() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    public void otherActuatorEndpointsAreNotExposed() throws Exception {
        mockMvc.perform(get("/actuator/env")).andExpect(status().is4xxClientError());
        mockMvc.perform(get("/actuator/beans")).andExpect(status().is4xxClientError());
    }
}
