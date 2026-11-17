package Ontdekstation013.ClimateChecker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.support.AbstractApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class StationSecurityApiTest extends AbstractApiTest {

    private static String claimBody(long stationId, String name) {
        return "{\"stationid\":" + stationId + ",\"name\":\"" + name + "\",\"is_public\":true,\"userid\":1}";
    }

    @Test
    void claimingAFreeStationAssignsItToTheCallerAndKeepsTheRegistrationCode() throws Exception {
        User caller = createUser(UserRole.USER);
        Station station = createStation(null);
        long registrationCode = station.getRegistrationCode();

        mockMvc.perform(put("/api/Meetstation/Claim")
                        .header(HttpHeaders.AUTHORIZATION, bearer(caller))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(claimBody(station.getStationid(), "Mijn station")))
                .andExpect(status().isOk());

        Station reloaded = stationRepository.findById(station.getStationid()).orElseThrow();
        assertThat(reloaded.getUserid()).isEqualTo(caller.getUserId());
        assertThat(reloaded.getName()).isEqualTo("Mijn station");
        assertThat(reloaded.getRegistrationCode()).isEqualTo(registrationCode);
    }

    @Test
    void claimingAStationThatAlreadyHasAnOwnerIsRejectedAndChangesNothing() throws Exception {
        User owner = createUser(UserRole.USER);
        User attacker = createUser(UserRole.USER);
        Station station = createStation(owner.getUserId());
        String originalName = station.getName();
        long registrationCode = station.getRegistrationCode();

        mockMvc.perform(put("/api/Meetstation/Claim")
                        .header(HttpHeaders.AUTHORIZATION, bearer(attacker))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(claimBody(station.getStationid(), "Overgenomen")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Station is al geclaimd"));

        Station reloaded = stationRepository.findById(station.getStationid()).orElseThrow();
        assertThat(reloaded.getUserid()).isEqualTo(owner.getUserId());
        assertThat(reloaded.getName()).isEqualTo(originalName);
        assertThat(reloaded.getRegistrationCode()).isEqualTo(registrationCode);
    }

    @Test
    void claimingAnUnknownStationGivesNotFound() throws Exception {
        User caller = createUser(UserRole.USER);

        mockMvc.perform(put("/api/Meetstation/Claim")
                        .header(HttpHeaders.AUTHORIZATION, bearer(caller))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(claimBody(424242424L, "Onbekend")))
                .andExpect(status().isNotFound());
    }

    @Test
    void addingAStationRequiresAnAdminRole() throws Exception {
        String body = "{\"stationid\":777001,\"name\":\"Nieuw\",\"registrationCode\":777001}";

        mockMvc.perform(post("/api/Meetstation/add")
                        .header(HttpHeaders.AUTHORIZATION, bearer(createUser(UserRole.USER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/Meetstation/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());

        assertThat(stationRepository.findById(777001L)).isEmpty();
    }

    @Test
    void anAdminCanAddAStationAndOnlyTheDtoFieldsAreUsed() throws Exception {
        // Extra velden (eigenaar, actief, publiek, foutvlaggen) zijn geen onderdeel van de DTO en worden genegeerd.
        String body = "{\"stationid\":777002,\"name\":\"Nieuw\",\"registrationCode\":777002,"
                + "\"userid\":1,\"isActive\":true,\"is_public\":true,\"database_tag\":\"X\",\"tempError\":true}";

        mockMvc.perform(post("/api/Meetstation/add")
                        .header(HttpHeaders.AUTHORIZATION, bearer(createUser(UserRole.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stationid").value(777002));

        Station saved = stationRepository.findById(777002L).orElseThrow();
        assertThat(saved.getUserid()).isNull();
        assertThat(saved.getIsActive()).isFalse();
        assertThat(saved.getIs_public()).isFalse();
        assertThat(saved.getDatabase_tag()).isEqualTo("MJS");
        assertThat(saved.getTempError()).isNull();
    }

    @Test
    void addingAStationFromTheFrontendFormDefaultsTheIdToTheRegistrationCode() throws Exception {
        // Het frontend-formulier stuurt geen stationid, een lege naam en tekstwaarden.
        mockMvc.perform(post("/api/Meetstation/add")
                        .header(HttpHeaders.AUTHORIZATION, bearer(createUser(UserRole.ADMIN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"database_tag\":\"MJS\",\"is_public\":false,\"is_active\":false,"
                                + "\"registrationCode\":\"777004\",\"location_locationid\":\"5\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stationid").value(777004));

        assertThat(stationRepository.findById(777004L).orElseThrow().getLocation_locationid()).isEqualTo(5L);
    }

    @Test
    void addingAStationWithoutRequiredFieldsOrWithADuplicateRegistrationCodeIsRejected() throws Exception {
        User admin = createUser(UserRole.SUPER_ADMIN);
        Station existing = createStation(null);

        mockMvc.perform(post("/api/Meetstation/add")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Zonder registratiecode\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/Meetstation/add")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stationid\":777003,\"name\":\"Dubbel\",\"registrationCode\":"
                                + existing.getRegistrationCode() + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Registratiecode is al in gebruik!"));
    }
}
