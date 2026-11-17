package Ontdekstation013.ClimateChecker.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.support.AbstractApiTest;
import org.junit.jupiter.api.Test;

class StationFilterApiTest extends AbstractApiTest {

    @Test
    void useridFilterOnlyReturnsStationsOfThatUser() throws Exception {
        User owner = createUser(UserRole.USER);
        User other = createUser(UserRole.USER);
        var mine1 = createStation(owner.getUserId());
        var mine2 = createStation(owner.getUserId());
        createStation(other.getUserId());
        createStation(null);

        mockMvc.perform(get("/api/Meetstation/stations").param("userid", String.valueOf(owner.getUserId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.stationid == " + mine1.getStationid() + ")]").exists())
                .andExpect(jsonPath("$[?(@.stationid == " + mine2.getStationid() + ")]").exists());
    }

    @Test
    void useridOfAUserWithoutStationsGivesAnEmptyListNotAllStations() throws Exception {
        createStation(null);
        User noStations = createUser(UserRole.USER);

        mockMvc.perform(get("/api/Meetstation/stations").param("userid", String.valueOf(noStations.getUserId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void useridAndUsernameAreCombinedWithAnd() throws Exception {
        User owner = createUser(UserRole.USER);
        createStation(owner.getUserId());

        // Same user id, but a username that does not belong to that user: no match.
        mockMvc.perform(get("/api/Meetstation/stations")
                        .param("userid", String.valueOf(owner.getUserId()))
                        .param("username", "Niemand Anders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void withoutPagingParametersAllStationsAreReturnedAsBefore() throws Exception {
        for (int i = 0; i < 25; i++) {
            createStation(null);
        }

        mockMvc.perform(get("/api/Meetstation/stations"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("X-Total-Count"))
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(25)));
    }

    @Test
    void pageAndPageSizeSliceTheResultAndReportTheTotal() throws Exception {
        User owner = createUser(UserRole.USER);
        for (int i = 0; i < 5; i++) {
            createStation(owner.getUserId());
        }
        String userid = String.valueOf(owner.getUserId());

        mockMvc.perform(get("/api/Meetstation/stations").param("userid", userid).param("pageSize", "2").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "5"))
                .andExpect(jsonPath("$.length()").value(2));

        // Last page holds the remainder.
        mockMvc.perform(get("/api/Meetstation/stations").param("userid", userid).param("pageSize", "2").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "5"))
                .andExpect(jsonPath("$.length()").value(1));

        // Beyond the last page: empty, not an error.
        mockMvc.perform(get("/api/Meetstation/stations").param("userid", userid).param("pageSize", "2").param("page", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void invalidPagingParametersGiveBadRequest() throws Exception {
        mockMvc.perform(get("/api/Meetstation/stations").param("page", "-1")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/Meetstation/stations").param("pageSize", "0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/Meetstation/stations").param("pageSize", "100000")).andExpect(status().isBadRequest());
    }
}
