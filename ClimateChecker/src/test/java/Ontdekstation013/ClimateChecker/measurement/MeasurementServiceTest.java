package Ontdekstation013.ClimateChecker.measurement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Ontdekstation013.ClimateChecker.features.measurement.LocalMeasurementReader;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDto;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MeasurementServiceTest {
    private MeetJeStadService meetJeStadService;
    private LocalMeasurementReader localMeasurementReader;
    private MeasurementService service;

    @BeforeEach
    public void setUp() {
        meetJeStadService = mock(MeetJeStadService.class);
        localMeasurementReader = mock(LocalMeasurementReader.class);
        service = new MeasurementService(meetJeStadService, localMeasurementReader, ZoneOffset.UTC);
    }

    private static Measurement measurement(Instant timestamp, Float temperature) {
        Station station = new Station();
        station.setStationid(5L);
        Measurement m = new Measurement(timestamp, 51.56f, 5.08f, temperature, 60f, 8f, 12f);
        m.setStation(station);
        return m;
    }

    @Test
    public void generatePdf_returnsPdfBytesForMeasurements() {
        byte[] pdf = service.generatePdf(List.of(measurement(Instant.parse("2026-01-01T12:00:00Z"), 15f)));

        assertTrue(new String(pdf, 0, 4, StandardCharsets.US_ASCII).startsWith("%PDF"));
    }

    @Test
    public void generatePdf_emptyList_returnsValidPdfInsteadOfFailing() {
        byte[] pdf = service.generatePdf(List.of());

        assertTrue(new String(pdf, 0, 4, StandardCharsets.US_ASCII).startsWith("%PDF"));
    }

    @Test
    public void generatePdf_handlesMeasurementWithoutTemperature() {
        byte[] pdf = service.generatePdf(List.of(measurement(Instant.parse("2026-01-01T12:00:00Z"), null)));

        assertTrue(pdf.length > 0);
    }

    @Test
    public void getMeasurementsAtTime_asksForTheMinuteMarginBeforeTheGivenTime() {
        Instant now = Instant.parse("2026-01-01T12:00:00Z");
        when(meetJeStadService.getMinuteLimit()).thenReturn(1440);
        when(localMeasurementReader.getMeasurements(any(), any(), anyBoolean()))
                .thenReturn(List.of(measurement(now, 15f)));

        List<MeasurementDto> result = service.getMeasurementsAtTime(now);

        verify(localMeasurementReader).getMeasurements(now.minusSeconds(1440 * 60L), now, false);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getId());
    }

    @Test
    public void getDailyMeasurements_convertsEntitiesToDtosWithDutchTimestamp() {
        Instant time = Instant.parse("2026-01-01T12:00:00Z");
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of(measurement(time, 15f)));

        List<MeasurementDto> result = service.GetDailyMeasurements(time);

        assertEquals(1, result.size());
        assertEquals("01-01-2026 13:00:00", result.get(0).getTimestamp());
        assertEquals(15f, result.get(0).getTemperature());
    }

    @Test
    public void getDailyMeasurements_returnsEmptyListWhenThereAreNoMeasurements() {
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of());

        assertTrue(service.GetDailyMeasurements(Instant.now()).isEmpty());
    }
}
