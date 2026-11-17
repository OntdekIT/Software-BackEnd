package Ontdekstation013.ClimateChecker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Ontdekstation013.ClimateChecker.features.station.endpoint.StationController;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.support.AbstractApiTest;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.CrossOrigin;

class AuthSecurityApiTest extends AbstractApiTest {

    private MvcResult forgotPassword(String email) throws Exception {
        return mockMvc.perform(post("/api/authentication/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andReturn();
    }

    @Test
    void forgotPasswordGivesTheSameReactionForKnownAndUnknownAddresses() throws Exception {
        User known = createUser(UserRole.USER);

        MvcResult knownResult = forgotPassword(known.getEmail());
        MvcResult unknownResult = forgotPassword("bestaat-niet@example.com");

        assertThat(knownResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(unknownResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(knownResult.getResponse().getContentAsString())
                .isEqualTo(unknownResult.getResponse().getContentAsString());
        verify(emailSenderService).sendForgotPasswordMail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void forgotPasswordDoesNotRevealAFailingMailServerForAKnownAddress() throws Exception {
        User known = createUser(UserRole.USER);
        doThrow(new MessagingException("smtp down"))
                .when(emailSenderService).sendForgotPasswordMail(anyString(), anyString(), anyString(), anyString());

        assertThat(forgotPassword(known.getEmail()).getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void forgotPasswordWithoutAnEmailStillGivesTheSameReaction() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/authentication/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        verify(emailSenderService, never()).sendForgotPasswordMail(anyString(), anyString(), anyString(), anyString());
    }

    private String registerBody(String password) {
        return "{\"firstName\":\"Test\",\"lastName\":\"Gebruiker\",\"email\":\"nieuw@example.com\","
                + "\"password\":\"" + password + "\",\"stationCode\":1,\"workshopCode\":1}";
    }

    @Test
    void registerRejectsWeakPasswords() throws Exception {
        for (String weak : new String[] {"kort1A", "alleenkleineletters1", "GeenCijfersHier", ""}) {
            mockMvc.perform(post("/api/authentication/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerBody(weak)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("password")));
        }
    }

    @Test
    void registerAcceptsAStrongPasswordAndProceedsToTheNextChecks() throws Exception {
        // Het wachtwoord is goed; de (niet-bestaande) workshopcode wordt daarna geweigerd.
        mockMvc.perform(post("/api/authentication/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("Sterk1Wachtwoord")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Ongeldige workshopcode"));
    }

    @Test
    void stationControllerHasNoHardcodedCrossOrigin() {
        assertThat(StationController.class.getAnnotation(CrossOrigin.class)).isNull();
    }

    @Test
    void corsIsHandledCentrallyForConfiguredOriginsOnly() throws Exception {
        mockMvc.perform(options("/api/Meetstation/stations")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));

        mockMvc.perform(options("/api/Meetstation/stations")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }
}
