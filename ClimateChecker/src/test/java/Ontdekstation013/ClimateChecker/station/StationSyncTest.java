package Ontdekstation013.ClimateChecker.station;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDto;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import Ontdekstation013.ClimateChecker.features.station.StationService;
import Ontdekstation013.ClimateChecker.features.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class StationSyncTest {
    private StationRepository stationRepository;
    private MeasurementService measurementService;
    private StationService stationService;

    @BeforeEach
    public void setUp() {
        stationRepository = mock(StationRepository.class);
        measurementService = mock(MeasurementService.class);
        stationService = new StationService(stationRepository, mock(UserRepository.class), measurementService);
    }

    private static MeasurementDto dto(int stationId, String timestamp, Float temperature) {
        MeasurementDto dto = new MeasurementDto();
        dto.setId(stationId);
        dto.setTimestamp(timestamp);
        dto.setLatitude(51.56f);
        dto.setLongitude(5.08f);
        dto.setTemperature(temperature);
        return dto;
    }

    @SuppressWarnings("unchecked")
    private List<Station> savedStations() {
        ArgumentCaptor<List<Station>> captor = ArgumentCaptor.forClass(List.class);
        verify(stationRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    public void sync_createsUnknownStationWithItsMeasurements() {
        when(measurementService.GetDailyMeasurements(any())).thenReturn(List.of(
                dto(11, "01-01-2026 12:00:00", 15f),
                dto(11, "01-01-2026 12:10:00", 16f)));
        when(stationRepository.findAllById(any())).thenReturn(List.of());
        when(stationRepository.findById(11L)).thenReturn(Optional.empty());

        stationService.UpdateStationsInDatabase(Instant.parse("2026-01-01T13:00:00Z"));

        List<Station> saved = savedStations();
        assertEquals(1, saved.size());
        assertEquals(11L, saved.get(0).getStationid());
        assertFalse(saved.get(0).getIs_public());
        assertEquals(2, saved.get(0).getMeasurementList().size());
    }

    @Test
    public void sync_skipsMeasurementsWithAnUnparsableTimestamp() {
        when(measurementService.GetDailyMeasurements(any())).thenReturn(List.of(
                dto(11, "dit-is-geen-datum", 15f),
                dto(11, "01-01-2026 12:10:00", 16f)));
        when(stationRepository.findAllById(any())).thenReturn(List.of());
        when(stationRepository.findById(11L)).thenReturn(Optional.empty());

        stationService.UpdateStationsInDatabase(Instant.parse("2026-01-01T13:00:00Z"));

        assertEquals(1, savedStations().get(0).getMeasurementList().size());
    }

    @Test
    public void sync_ignoresDuplicateMeasurementsForTheSameTimestamp() {
        when(measurementService.GetDailyMeasurements(any())).thenReturn(List.of(
                dto(11, "01-01-2026 12:00:00", 15f),
                dto(11, "01-01-2026 12:00:00", 15f)));
        when(stationRepository.findAllById(any())).thenReturn(List.of());
        when(stationRepository.findById(11L)).thenReturn(Optional.empty());

        stationService.UpdateStationsInDatabase(Instant.parse("2026-01-01T13:00:00Z"));

        assertEquals(1, savedStations().get(0).getMeasurementList().size());
    }

    @Test
    public void sync_keepsExistingStationAndGroupsMeasurementsPerStation() {
        Station existing = new Station();
        existing.setStationid(12L);
        existing.setName("Bestaand");
        existing.setRegistrationCode(12L);
        existing.setIs_public(true);
        existing.setIsActive(true);
        existing.setTempError(false);
        existing.setHumError(false);
        existing.setStofError(false);
        existing.setLocError(false);
        when(measurementService.GetDailyMeasurements(any())).thenReturn(List.of(
                dto(12, "01-01-2026 12:00:00", 15f),
                dto(13, "01-01-2026 12:00:00", 17f)));
        when(stationRepository.findAllById(any())).thenReturn(List.of(existing));
        when(stationRepository.findById(12L)).thenReturn(Optional.of(existing));
        when(stationRepository.findById(13L)).thenReturn(Optional.empty());

        stationService.UpdateStationsInDatabase(Instant.parse("2026-01-01T13:00:00Z"));

        List<Station> saved = savedStations();
        assertEquals(2, saved.size());
        assertTrue(saved.stream().allMatch(s -> s.getMeasurementList().size() == 1));
        assertTrue(saved.stream().anyMatch(s -> "Bestaand".equals(s.getName())));
    }

    @Test
    public void sync_withoutMeasurements_savesNothingNew() {
        when(measurementService.GetDailyMeasurements(any())).thenReturn(List.of());
        when(stationRepository.findAllById(any())).thenReturn(List.of());

        stationService.UpdateStationsInDatabase(Instant.parse("2026-01-01T13:00:00Z"));

        assertTrue(savedStations().isEmpty());
        verify(stationRepository, never()).save(any());
    }

    @Test
    public void sync_doesNotThrowWhenSavingFails() {
        when(measurementService.GetDailyMeasurements(any())).thenReturn(List.of(dto(11, "01-01-2026 12:00:00", 15f)));
        when(stationRepository.findAllById(any())).thenReturn(List.of());
        when(stationRepository.findById(11L)).thenReturn(Optional.empty());
        when(stationRepository.saveAll(any())).thenThrow(new RuntimeException("db weg"));

        stationService.UpdateStationsInDatabase(Instant.parse("2026-01-01T13:00:00Z"));

        verify(stationRepository).saveAll(any());
    }
}
