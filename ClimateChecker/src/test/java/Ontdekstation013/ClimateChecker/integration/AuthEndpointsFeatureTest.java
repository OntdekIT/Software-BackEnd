package Ontdekstation013.ClimateChecker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.mail.internet.MimeMessage;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Feature-tests door de echte HTTP-laag (MockMvc) tegen de volledige applicatie
 * met een echte database (Testcontainers). Test het samenspel van security,
 * controller, service en database — niet in isolatie.
 */
@AutoConfigureMockMvc
public class AuthEndpointsFeatureTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Geen echte SMTP-server in de test; mock de mailsender zodat de
    // verificatiemail "verstuurd" wordt zonder verbinding.
    @MockBean
    private JavaMailSender javaMailSender;

    @org.junit.jupiter.api.BeforeEach
    void stubMailSender() {
        // createMimeMessage() moet een echt MimeMessage geven (anders NPE in
        // de MimeMessageHelper); send() is een no-op op de mock.
        MimeMessage message = new org.springframework.mail.javamail.JavaMailSenderImpl().createMimeMessage();
        org.mockito.Mockito.when(javaMailSender.createMimeMessage()).thenReturn(message);
    }

    @Test
    public void usersEndpoint_isForbidden_withoutAuthentication() throws Exception {
        // /api/users mag alleen door (super)admins; zonder token -> 403.
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void publicStationsEndpoint_isReachable_withoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/Meetstation/stations"))
                .andExpect(status().isOk());
    }

    @Test
    public void login_withWrongPassword_returnsBadRequest() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "admin@example.com", "password", "verkeerd"));

        mockMvc.perform(post("/api/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_withSeededAdmin_requiresVerification() throws Exception {
        // De seed-admin bestaat in de database (via de DataSeeder). Een login
        // vanaf een onbekend IP vraagt om mailverificatie (verificationRequired).
        String body = objectMapper.writeValueAsString(
                Map.of("email", "admin@example.com", "password", "Admin123!"));

        mockMvc.perform(post("/api/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationRequired", is(true)));
    }
}
