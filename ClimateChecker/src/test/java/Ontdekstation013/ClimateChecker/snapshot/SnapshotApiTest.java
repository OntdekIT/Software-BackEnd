package Ontdekstation013.ClimateChecker.snapshot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Ontdekstation013.ClimateChecker.features.measurement.LocalMeasurementReader;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementRepository;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.Neighbourhood;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodCoords;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodCoordsRepository;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodRepository;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.ingest.IngestState;
import Ontdekstation013.ClimateChecker.support.AbstractApiTest;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

/**
 * De endpoints bedienen vooraf berekende snapshots. De externe bron is hier een mock die faalt als
 * hij wordt aangeroepen: een verzoek mag nooit naar meetjestad.net gaan.
 */
class SnapshotApiTest extends AbstractApiTest {

    @MockBean
    private MeetJeStadService meetJeStadService;

    @SpyBean
    private LocalMeasurementReader reader;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private IngestState ingestState;

    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private NeighbourhoodRepository neighbourhoodRepository;

    @Autowired
    private NeighbourhoodCoordsRepository coordsRepository;

    private Station station;

    @BeforeEach
    void fixture() {
        when(meetJeStadService.getMinuteLimit()).thenReturn(1440);
        when(meetJeStadService.IncorrectValueFilter(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(meetJeStadService.getMeasurements(any(MeetJeStadParameters.class)))
                .thenThrow(new AssertionError("meetjestad.net mag niet vanuit een verzoek worden aangeroepen"));

        Neighbourhood wijk = neighbourhoodRepository.save(new Neighbourhood(7001, "Testwijk"));
        float[][] corners = {{51.50f, 5.00f}, {51.60f, 5.00f}, {51.60f, 5.20f}, {51.50f, 5.20f}};
        for (int i = 0; i < corners.length; i++) {
            coordsRepository.save(new NeighbourhoodCoords(7001L * 10 + i, corners[i][0], corners[i][1], wijk));
        }
        station = createStation(null);
        station.setName("Teststation");
        stationRepository.save(station);
        addMeasurement(station, Duration.ofMinutes(10), 12.5f);
        addMeasurement(station, Duration.ofHours(3), 9f);
        entityManagerFlush();
        snapshotService.rebuild();
        clearInvocations(reader);
    }

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private void entityManagerFlush() {
        entityManager.flush();
        entityManager.clear();
    }

    private void addMeasurement(Station target, Duration ago, float temperature) {
        Measurement m = new Measurement(Instant.now().minus(ago), 51.55f, 5.10f, temperature, 55f, 6f, 9f);
        m.setStation(target);
        measurementRepository.save(m);
    }

    @Test
    void snapshotEndpointReturnsTheConsolidatedState() throws Exception {
        mockMvc.perform(get("/api/snapshot"))
                .andExpect(status().isOk())
                .andExpect(header().exists("ETag"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=30, public"))
                .andExpect(header().exists("X-Data-As-Of"))
                .andExpect(jsonPath("$.schemaVersion").value(1))
                .andExpect(jsonPath("$.recencyMinutes").value(120))
                .andExpect(jsonPath("$.wijken[?(@.name=='Testwijk')].avgTemp").value(12.5))
                .andExpect(jsonPath("$.wijken[?(@.name=='Testwijk')].series.length()").value(24))
                .andExpect(jsonPath("$.stations[?(@.id==" + station.getStationid() + ")].status").value("ok"))
                .andExpect(jsonPath("$.stations[?(@.id==" + station.getStationid() + ")].latest.temperature").value(12.5))
                .andExpect(jsonPath("$.network.stationsTotal").isNumber())
                .andExpect(jsonPath("$.heatmapPoints.length()").value(1))
                .andExpect(jsonPath("$.city.warmest.name").value("Testwijk"));
    }

    @Test
    void anEtagMatchGivesNotModifiedWithoutBody() throws Exception {
        String etag = mockMvc.perform(get("/api/snapshot")).andReturn().getResponse().getHeader("ETag");

        mockMvc.perform(get("/api/snapshot").header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified())
                .andExpect(header().string("ETag", etag));
        mockMvc.perform(get("/api/snapshot").header(HttpHeaders.IF_NONE_MATCH, "W/\"iets-anders\""))
                .andExpect(status().isOk());
    }

    @Test
    void theEtagStaysTheSameForUnchangedDataAndChangesWhenDataChanges() throws Exception {
        String before = mockMvc.perform(get("/api/snapshot")).andReturn().getResponse().getHeader("ETag");

        snapshotService.rebuild();
        String afterRebuildWithoutChange = mockMvc.perform(get("/api/snapshot")).andReturn().getResponse().getHeader("ETag");
        addMeasurement(station, Duration.ofMinutes(2), 20f);
        entityManagerFlush();
        snapshotService.rebuild();
        String afterNewData = mockMvc.perform(get("/api/snapshot")).andReturn().getResponse().getHeader("ETag");

        assertThat(afterRebuildWithoutChange).isEqualTo(before);
        assertThat(afterNewData).isNotEqualTo(before);
    }

    @Test
    void legacyEndpointsForNowAreServedFromTheSnapshotWithoutTouchingTheDatabaseOrTheSource() throws Exception {
        String now = Instant.now().toString();

        mockMvc.perform(get("/api/neighbourhood/history").param("timestamp", now))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Testwijk')].avgTemp").value(12.5))
                .andExpect(jsonPath("$[0].coordinates").isArray());
        mockMvc.perform(get("/api/neighbourhood/history/timelapse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frames.length()").value(24))
                .andExpect(jsonPath("$.neighbourhoods").isArray());
        mockMvc.perform(get("/api/neighbourhood/history/timelapse").param("steps", "25"))
                .andExpect(jsonPath("$.frames.length()").value(25));
        MvcResult async = mockMvc.perform(get("/api/meetstation/stations-met-measurements").param("timestamp", now))
                .andExpect(request().asyncStarted()).andReturn();
        mockMvc.perform(asyncDispatch(async))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Data-As-Of"))
                .andExpect(jsonPath("$[?(@.stationid==" + station.getStationid() + ")].measurements.length()").value(1));

        verify(reader, never()).getMeasurements(any(Instant.class), any(Instant.class), anyBoolean());
        verify(reader, never()).getMeasurements(any(MeetJeStadParameters.class));
        verify(meetJeStadService, never()).getMeasurements(any(MeetJeStadParameters.class));
    }

    @Test
    void aRequestForAnotherMomentIsComputedFromTheLocalDatabaseNotTheSource() throws Exception {
        String longAgo = Instant.now().minus(Duration.ofDays(3)).toString();

        mockMvc.perform(get("/api/neighbourhood/history").param("timestamp", longAgo)).andExpect(status().isOk());

        verify(reader).getMeasurements(any(Instant.class), any(Instant.class), anyBoolean());
        verify(meetJeStadService, never()).getMeasurements(any(MeetJeStadParameters.class));
    }

    @Test
    void historicalEndpointsAlsoReadLocally() throws Exception {
        mockMvc.perform(get("/api/measurement/history/average/" + station.getStationid())
                        .param("startDate", "01-01-2025 12:00").param("endDate", "02-01-2025 12:00"))
                .andExpect(status().isOk());

        verify(meetJeStadService, never()).getMeasurements(any(MeetJeStadParameters.class));
    }

    @Test
    void theFreshnessHeaderIsTheLastSuccessfulIngest() throws Exception {
        Instant success = Instant.parse("2026-03-01T11:58:00Z");
        ingestState.recordSuccess(success, 5);

        mockMvc.perform(get("/api/neighbourhood/history").param("timestamp", Instant.now().toString()))
                .andExpect(header().string("X-Data-As-Of", success.toString()));
        mockMvc.perform(get("/api/snapshot")).andExpect(header().string("X-Data-As-Of", success.toString()));
    }

    @Test
    void theFreshnessHeaderIsExposedToBrowsersOnCrossOriginRequests() throws Exception {
        mockMvc.perform(get("/api/snapshot").header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                        org.hamcrest.Matchers.containsString("X-Data-As-Of")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                        org.hamcrest.Matchers.containsString("ETag")));
    }

    @Test
    void theStatusEndpointShowsIngestStateAndSnapshotInfo() throws Exception {
        ingestState.recordAttempt(Instant.parse("2026-03-01T11:59:00Z"));
        ingestState.recordFailure("ResourceAccessException: time-out");

        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingest.enabled").value(false))
                .andExpect(jsonPath("$.ingest.consecutiveFailures").value(1))
                .andExpect(jsonPath("$.ingest.lastError").value("ResourceAccessException: time-out"))
                .andExpect(jsonPath("$.snapshot.version").isNumber())
                .andExpect(jsonPath("$.snapshot.wijken").isNumber());
    }

    @Test
    void anEmptyDatabaseGivesEmptyArraysWithoutCallingTheSource() throws Exception {
        measurementRepository.deleteAll();
        stationRepository.deleteAll();
        neighbourhoodRepository.findAll().forEach(n -> {
            coordsRepository.deleteAll(coordsRepository.findAll().stream().filter(c -> c.getNeighbourhood().getId() == n.getId()).toList());
            neighbourhoodRepository.delete(n);
        });
        entityManagerFlush();
        snapshotService.rebuild();

        mockMvc.perform(get("/api/snapshot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wijken.length()").value(0))
                .andExpect(jsonPath("$.stations.length()").value(0))
                .andExpect(jsonPath("$.heatmapPoints.length()").value(0));
        mockMvc.perform(get("/api/neighbourhood/history").param("timestamp", Instant.now().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        verify(meetJeStadService, never()).getMeasurements(any(MeetJeStadParameters.class));
    }

    @Test
    void readersNeverSeeAHalfBuiltSnapshotWhileItIsBeingSwapped() throws Exception {
        AtomicBoolean stop = new AtomicBoolean();
        AtomicReference<String> problem = new AtomicReference<>();
        Thread reading = new Thread(() -> {
            long lastVersion = 0;
            while (!stop.get()) {
                MapSnapshot snapshot = snapshotService.current();
                // Binnen een snapshot horen alle onderdelen bij elkaar, en versies gaan alleen vooruit.
                if (!snapshot.overview().generatedAt().equals(snapshot.builtAt().toString())
                        || snapshot.overviewJson().length == 0 || snapshot.etag() == null
                        || snapshot.stations() == null || snapshot.neighbourhoods() == null
                        || snapshot.timelapses().size() != 2 || snapshot.version() < lastVersion) {
                    problem.compareAndSet(null, "inconsistente snapshot v" + snapshot.version());
                }
                lastVersion = snapshot.version();
            }
        });
        reading.start();
        long before = snapshotService.current().version();
        for (int i = 0; i < 5; i++) {
            snapshotService.rebuild();
        }
        stop.set(true);
        reading.join(5000);

        assertThat(problem.get()).isNull();
        assertThat(snapshotService.current().version()).isEqualTo(before + 5);
    }
}
