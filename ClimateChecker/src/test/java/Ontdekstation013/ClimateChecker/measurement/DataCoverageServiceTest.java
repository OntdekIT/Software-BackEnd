package Ontdekstation013.ClimateChecker.measurement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @Test
    public void rejectsDayPeriodThatExceedsMaximumAfterAligning() {
        // 31 days from 12:00 local time; aligned to the start of that day it is longer than 31 days.
        Instant from = Instant.parse("2026-01-01T11:00:00Z");
        Instant to = from.plus(DataCoverageService.MAX_RANGE);

        assertThrows(InvalidArgumentException.class, () -> service.getPmCoverage(from, to, "day"));
        verifyNoInteractions(meetJeStadService);
    }

    @Test
    public void fetchesLongPeriodsInChunks() {
        Station station = new Station();
        station.setStationid(42L);
        when(stationRepository.findAll()).thenReturn(List.of(station));
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of());

        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = from.plus(Duration.ofDays(30));
        service.getPmCoverage(from, to, "hour");

        ArgumentCaptor<MeetJeStadParameters> captor = ArgumentCaptor.forClass(MeetJeStadParameters.class);
        verify(meetJeStadService, times(5)).getMeasurements(captor.capture());
        List<MeetJeStadParameters> chunks = captor.getAllValues();
        assertEquals(from, chunks.get(0).StartDate);
        assertEquals(to, chunks.get(4).EndDate);
        for (int i = 1; i < chunks.size(); i++) {
            assertEquals(chunks.get(i - 1).EndDate, chunks.get(i).StartDate);
        }
    }

    @Test
    public void emptyDatabaseDoesNotQueryMeetJeStad() {
        when(stationRepository.findAll()).thenReturn(List.of());

        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        DataCoverageResponse response = service.getPmCoverage(from, from.plus(Duration.ofDays(1)), "hour");

        verify(meetJeStadService, never()).getMeasurements(any());
        assertEquals(0, response.getStationCount());
    }

    @Test
    public void repeatedRequestIsServedFromCache() {
        Station station = new Station();
        station.setStationid(42L);
        when(stationRepository.findAll()).thenReturn(List.of(station));
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of());

        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = from.plus(Duration.ofDays(1));
        DataCoverageResponse first = service.getPmCoverage(from, to, "hour");
        DataCoverageResponse second = service.getPmCoverage(from, to, "hour");

        verify(meetJeStadService, times(1)).getMeasurements(any());
        assertEquals(first, second);
        service.getPmCoverage(from, to, "day");
        verify(meetJeStadService, times(2)).getMeasurements(any());
    }
}
