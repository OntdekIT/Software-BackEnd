package Ontdekstation013.ClimateChecker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Ontdekstation013.ClimateChecker.config.JwtSecretGuard;
import Ontdekstation013.ClimateChecker.exception.GlobalExceptionHandler;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.support.AbstractApiTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class ErrorHandlingApiTest extends AbstractApiTest {

    private static void assertStandardError(ResultActions result, int statusCode, String error) throws Exception {
        result.andExpect(status().is(statusCode))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.statusCode").value(statusCode))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    private String adminAuth() {
        return bearer(createUser(UserRole.ADMIN));
    }

    // --- /api/users: 404 and 403 instead of 500 ---

    @Test
    void unknownUserGivesNotFound() throws Exception {
        assertStandardError(mockMvc.perform(get("/api/users/987654321")
                .header(HttpHeaders.AUTHORIZATION, adminAuth())), 404, "NOT_FOUND");
    }

    @Test
    void adminCannotAssignTheSuperAdminRole() throws Exception {
        User target = createUser(UserRole.USER);

        assertStandardError(mockMvc.perform(put("/api/users/" + target.getUserId())
                .header(HttpHeaders.AUTHORIZATION, adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"SUPER_ADMIN\"}")), 403, "FORBIDDEN");

        assertThat(userRepository.findById(target.getUserId()).orElseThrow().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void superAdminCanStillChangeRoles() throws Exception {
        User target = createUser(UserRole.USER);

        mockMvc.perform(put("/api/users/" + target.getUserId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(createUser(UserRole.SUPER_ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isOk());

        assertThat(userRepository.findById(target.getUserId()).orElseThrow().getRole()).isEqualTo(UserRole.SUPER_ADMIN);
    }

    @Test
    void adminCannotDeleteAUserWithASpecialRoleAndDeletingAnUnknownUserIsNotFound() throws Exception {
        User otherAdmin = createUser(UserRole.ADMIN);

        assertStandardError(mockMvc.perform(delete("/api/users/" + otherAdmin.getUserId())
                .header(HttpHeaders.AUTHORIZATION, adminAuth())), 403, "FORBIDDEN");
        assertThat(userRepository.findById(otherAdmin.getUserId())).isPresent();

        assertStandardError(mockMvc.perform(delete("/api/users/987654321")
                .header(HttpHeaders.AUTHORIZATION, adminAuth())), 404, "NOT_FOUND");
    }

    // --- invalid dates: 400 instead of 500 ---

    @Test
    void invalidDatesOnHistoryAverageGiveBadRequest() throws Exception {
        assertStandardError(mockMvc.perform(get("/api/measurement/history/average/1")
                .param("startDate", "gisteren").param("endDate", "31-12-2025 10:00")), 400, "BAD_REQUEST");
        // Valid format but impossible day of month.
        assertStandardError(mockMvc.perform(get("/api/measurement/history/average/1")
                .param("startDate", "32-01-2025 10:00").param("endDate", "01-03-2025 10:00")), 400, "BAD_REQUEST");
    }

    @Test
    void startAfterEndOnHistoryAverageStaysBadRequest() throws Exception {
        assertStandardError(mockMvc.perform(get("/api/measurement/history/average/1")
                .param("startDate", "02-01-2025 10:00").param("endDate", "01-01-2025 10:00")), 400, "BAD_REQUEST");
    }

    @Test
    void invalidDatesOnStationMeasurementsGiveBadRequest() throws Exception {
        assertStandardError(mockMvc.perform(get("/api/Meetstation/measurements/1")
                .param("startDate", "2025-01-01").param("endDate", "2025-01-02")), 400, "BAD_REQUEST");
    }

    @Test
    void invalidTimestampOnStationsMetMeasurementsGivesBadRequest() throws Exception {
        assertStandardError(mockMvc.perform(get("/api/Meetstation/stationsMetMeasurements")
                .param("timestamp", "morgen")), 400, "BAD_REQUEST");
    }

    @Test
    void workshopWithAnInvalidOrMissingExpirationDateGivesBadRequest() throws Exception {
        String auth = adminAuth();
        for (String body : new String[] {"{\"expirationDate\":\"volgende week\"}", "{}", "{\"expirationDate\":\"\"}"}) {
            assertStandardError(mockMvc.perform(post("/api/workshops")
                    .header(HttpHeaders.AUTHORIZATION, auth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)), 400, "BAD_REQUEST");
        }
    }

    @Test
    void workshopWithAValidExpirationDateIsCreated() throws Exception {
        mockMvc.perform(post("/api/workshops")
                        .header(HttpHeaders.AUTHORIZATION, adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expirationDate\":\"2099-01-01T10:00:00\"}"))
                .andExpect(status().isCreated());
    }

    // --- 401 and 403 with the standard body ---

    @Test
    void protectedEndpointWithoutTokenGivesUnauthorizedWithStandardBody() throws Exception {
        assertStandardError(mockMvc.perform(get("/api/users")), 401, "UNAUTHORIZED");
        assertStandardError(mockMvc.perform(get("/api/my-account")), 401, "UNAUTHORIZED");
    }

    @Test
    void insufficientRoleGivesForbiddenWithStandardBody() throws Exception {
        assertStandardError(mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, bearer(createUser(UserRole.USER)))), 403, "FORBIDDEN");
    }

    @Test
    void garbageExpiredAndOrphanedTokensGiveUnauthorizedInsteadOfAServerError() throws Exception {
        assertStandardError(mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dit-is-geen-jwt")), 401, "UNAUTHORIZED");

        User user = createUser(UserRole.ADMIN);
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JwtSecretGuard.KNOWN_DEFAULT_SECRET));
        String expired = Jwts.builder().setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 7_200_000))
                .setExpiration(new Date(System.currentTimeMillis() - 3_600_000))
                .signWith(key).compact();
        assertStandardError(mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expired)), 401, "UNAUTHORIZED");

        String orphan = Jwts.builder().setSubject("987654321")
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key).compact();
        assertStandardError(mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + orphan)), 401, "UNAUTHORIZED");
    }

    @Test
    void anInvalidTokenOnAPublicEndpointDoesNotBreakTheRequest() throws Exception {
        mockMvc.perform(get("/api/Meetstation/stations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dit-is-geen-jwt"))
                .andExpect(status().isOk());
    }

    // --- central handler ---

    @Test
    void springMvcErrorsUseTheStandardBody() throws Exception {
        assertStandardError(mockMvc.perform(get("/api/measurement/history")), 400, "BAD_REQUEST");
        assertStandardError(mockMvc.perform(delete("/api/Meetstation/stations")), 405, "METHOD_NOT_ALLOWED");
        assertStandardError(mockMvc.perform(post("/api/workshops")
                .header(HttpHeaders.AUTHORIZATION, adminAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{niet-json")), 400, "BAD_REQUEST");
    }

    @Test
    void unexpectedExceptionsBecomeAGenericServerErrorWithoutLeakingDetails() {
        var response = new GlobalExceptionHandler()
                .handleUnexpectedException(new IllegalStateException("jdbc:mariadb://db:3306 wachtwoord=geheim"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().statusCode()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().message()).doesNotContain("geheim");
    }
}
