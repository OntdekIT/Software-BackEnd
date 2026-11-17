package Ontdekstation013.ClimateChecker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationController;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.support.AbstractApiTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Canonical paths are lowercase and the typo is fixed; the old spellings keep working as aliases
 * so the frontend does not break, and they must be exactly as protected as the new ones.
 */
class PathAliasApiTest extends AbstractApiTest {

    @MockBean
    private MeetJeStadService meetJeStadService;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private MvcResult call(String path) throws Exception {
        return mockMvc.perform(get(path).param("timestamp", "2025-07-01T10:00:00Z")).andReturn();
    }

    private void assertSameAnswer(String canonical, String legacy) throws Exception {
        MvcResult newPath = call(canonical);
        MvcResult oldPath = call(legacy);

        assertThat(newPath.getResponse().getStatus()).as(canonical).isEqualTo(200);
        assertThat(oldPath.getResponse().getStatus()).as(legacy).isEqualTo(200);
        assertThat(oldPath.getResponse().getContentAsString()).isEqualTo(newPath.getResponse().getContentAsString());
    }

    @Test
    void stationsAnswerOnTheNewAndTheOldBasePath() throws Exception {
        createStation(null);

        assertSameAnswer("/api/meetstation/stations", "/api/Meetstation/stations");
    }

    @Test
    void availabilityAnswersOnTheFixedAndTheMisspelledPath() throws Exception {
        Station free = createStation(null);
        String suffix = "/" + free.getStationid() + "/123456";

        assertSameAnswer("/api/meetstation/availability" + suffix, "/api/Meetstation/Availibility" + suffix);
        // Unknown workshop code on a free station: 403 in the body, as before.
        assertThat(call("/api/meetstation/availability" + suffix).getResponse().getContentAsString()).isEqualTo("403");
    }

    @Test
    void stationsWithMeasurementsAnswersOnTheNewAndTheOldPath() throws Exception {
        when(meetJeStadService.getMeasurements(any(MeetJeStadParameters.class))).thenReturn(List.of());

        assertSameAnswer("/api/meetstation/stations-met-measurements", "/api/Meetstation/stationsMetMeasurements");
    }

    @Test
    void claimWorksOnTheNewAndTheOldPath() throws Exception {
        User caller = createUser(UserRole.USER);

        for (String path : new String[] {"/api/meetstation/claim", "/api/Meetstation/Claim"}) {
            Station free = createStation(null);
            mockMvc.perform(put(path)
                            .header(HttpHeaders.AUTHORIZATION, bearer(caller))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"stationid\":" + free.getStationid() + ",\"name\":\"Mijn station\",\"is_public\":true}"))
                    .andExpect(status().isOk());

            assertThat(stationRepository.findById(free.getStationid()).orElseThrow().getUserid())
                    .as(path).isEqualTo(caller.getUserId());
        }
    }

    @Test
    void everySpellingOfAStateChangingStationPathRequiresLogin() throws Exception {
        // Class-level and method-level aliases multiply (for example /api/meetstation/Claim and
        // /api/Meetstation/claim). Every combination that is mapped must be protected.
        Set<HttpMethod> readOnly = Set.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS);
        List<String> unprotected = new ArrayList<>();
        int checked = 0;

        for (var entry : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handler = entry.getValue();
            if (handler.getBeanType() != StationController.class || info.getPathPatternsCondition() == null) {
                continue;
            }
            for (var method : info.getMethodsCondition().getMethods()) {
                if (readOnly.contains(HttpMethod.valueOf(method.name()))) {
                    continue;
                }
                for (String pattern : info.getPathPatternsCondition().getPatternValues()) {
                    String path = pattern.replaceAll("\\{[^}]*}", "1");
                    MvcResult result = mockMvc.perform(
                                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                            .request(HttpMethod.valueOf(method.name()), path)
                                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                            .andReturn();
                    checked++;
                    int statusCode = result.getResponse().getStatus();
                    if (statusCode != 401 && statusCode != 403) {
                        unprotected.add(method + " " + path + " -> " + statusCode);
                    }
                }
            }
        }

        assertThat(checked).as("mapped state-changing station paths").isGreaterThanOrEqualTo(10);
        assertThat(unprotected).isEmpty();
    }

    @Test
    void publicReadsOfTheNewPathsDoNotRequireLogin() throws Exception {
        mockMvc.perform(get("/api/meetstation/stations")).andExpect(status().isOk());
        mockMvc.perform(get("/api/meetstation/availability/1/1")).andExpect(status().isOk()).andExpect(content().string("402"));
    }
}
