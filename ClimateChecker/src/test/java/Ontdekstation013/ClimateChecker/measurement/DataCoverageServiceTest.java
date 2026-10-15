package Ontdekstation013.ClimateChecker.measurement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.measurement.DataCoverageService;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import Ontdekstation013.ClimateChecker.utility.DataCoverageResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class DataCoverageServiceTest {

    private MeetJeStadService meetJeStadService;
    private StationRepository stationRepository;
    private DataCoverageService service;

    @BeforeEach
    public void setUp() {
        meetJeStadService = mock(MeetJeStadService.class);
        stationRepository = mock(StationRepository.class);
        service = new DataCoverageService(meetJeStadService, stationRepository);
    }

    @Test
    public void rejectsPeriodLongerThanMaximum() {
        Instant to = Instant.parse("2026-02-01T00:00:00Z");
        Instant from = to.minus(DataCoverageService.MAX_RANGE).minus(Duration.ofHours(1));

        assertThrows(InvalidArgumentException.class, () -> service.getPmCoverage(from, to, "hour"));
        verifyNoInteractions(meetJeStadService);
    }

    @Test
    public void rejectsFromAfterTo() {
        Instant from = Instant.parse("2026-02-01T00:00:00Z");

        assertThrows(InvalidArgumentException.class, () -> service.getPmCoverage(from, from.minusSeconds(3600), "hour"));
    }

    @Test
    public void requestsRawReadingsForKnownStations() {
        Station station = new Station();
        station.setStationid(42L);
        when(stationRepository.findAll()).thenReturn(List.of(station));
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of());

        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        DataCoverageResponse response = service.getPmCoverage(from, to, "hour");

        ArgumentCaptor<MeetJeStadParameters> captor = ArgumentCaptor.forClass(MeetJeStadParameters.class);
        verify(meetJeStadService).getMeasurements(captor.capture());
        assertEquals(List.of(42), captor.getValue().StationIds);
        assertEquals(true, captor.getValue().includeFaultyMeasurements);
        assertEquals(24, response.getBuckets().size());
        assertEquals("NO_DATA", response.getStations().get(0).getStatus());
    }
}
