package Ontdekstation013.ClimateChecker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.support.AbstractApiTest;
import java.time.Instant;
import java.util.List;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

/** The dd-MM-yyyy HH:mm endpoints must use application.timezone, not the zone of the JVM or host. */
@TestPropertySource(properties = "application.timezone=Europe/Amsterdam")
class TimeZoneApiTest extends AbstractApiTest {

    @MockBean
    private MeetJeStadService meetJeStadService;

    private TimeZone originalDefault;

    @BeforeEach
    void switchJvmZoneAwayFromTheConfiguredOne() {
        originalDefault = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland"));
        when(meetJeStadService.getMeasurements(any(MeetJeStadParameters.class))).thenReturn(List.<Measurement>of());
    }

    @AfterEach
    void restoreJvmZone() {
        TimeZone.setDefault(originalDefault);
    }

    private MeetJeStadParameters capturedParameters() {
        ArgumentCaptor<MeetJeStadParameters> captor = ArgumentCaptor.forClass(MeetJeStadParameters.class);
        // The background station monitor may call the service as well; pick the call for our station.
        verify(meetJeStadService, atLeastOnce()).getMeasurements(captor.capture());
        return captor.getAllValues().stream()
                .filter(params -> params.StationIds.contains(7))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void historyAverageInterpretsDatesInTheConfiguredZone() throws Exception {
        mockMvc.perform(get("/api/measurement/history/average/7")
                        .param("startDate", "01-07-2025 12:00").param("endDate", "02-07-2025 12:00"))
                .andExpect(status().isOk());

        // 12:00 Amsterdam in July (CEST, UTC+2) is 10:00 UTC; the JVM default (Auckland) is ignored.
        MeetJeStadParameters params = capturedParameters();
        assertThat(params.StartDate).isEqualTo(Instant.parse("2025-07-01T10:00:00Z"));
        assertThat(params.EndDate).isEqualTo(Instant.parse("2025-07-02T10:00:00Z"));
    }

    @Test
    void stationMeasurementsPdfInterpretsDatesInTheConfiguredZone() throws Exception {
        mockMvc.perform(get("/api/Meetstation/measurements/7")
                        .param("startDate", "01-01-2025 12:00").param("endDate", "02-01-2025 12:00"))
                .andExpect(status().isOk());

        // January is CET (UTC+1).
        MeetJeStadParameters params = capturedParameters();
        assertThat(params.StartDate).isEqualTo(Instant.parse("2025-01-01T11:00:00Z"));
        assertThat(params.EndDate).isEqualTo(Instant.parse("2025-01-02T11:00:00Z"));
    }
}
