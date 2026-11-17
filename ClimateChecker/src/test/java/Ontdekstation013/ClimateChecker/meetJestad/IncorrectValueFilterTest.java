package Ontdekstation013.ClimateChecker.meetJestad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class IncorrectValueFilterTest {

    private static MeetJeStadService service(float pm25Max, float pm10Max) {
        return new MeetJeStadService(Mockito.mock(StationRepository.class), "http://localhost",
                0f, 100f, 0f, pm25Max, 0f, pm10Max);
    }

    private static Measurement m(Float temp, Float hum, Float pm25, Float pm10) {
        return new Measurement(Instant.parse("2026-01-01T12:00:00Z"), 51.55f, 5f, temp, hum, pm25, pm10);
    }

    @Test
    public void validValuesAreKept() {
        List<Measurement> result = service(1000f, 1000f).IncorrectValueFilter(List.of(m(10f, 50f, 12f, 20f)));

        assertEquals(50f, result.get(0).getHumidity());
        assertEquals(12f, result.get(0).getPm25());
        assertEquals(20f, result.get(0).getPm10());
    }

    @Test
    public void humidityOutsideConfiguredLimitsBecomesNull() {
        List<Measurement> result = service(1000f, 1000f).IncorrectValueFilter(
                List.of(m(10f, 120f, null, null), m(10f, -1f, null, null), m(10f, 100f, null, null)));

        assertNull(result.get(0).getHumidity());
        assertNull(result.get(1).getHumidity());
        assertEquals(100f, result.get(2).getHumidity());
    }

    @Test
    public void particulateMatterOutsideLimitsBecomesNull() {
        List<Measurement> result = service(500f, 800f).IncorrectValueFilter(
                List.of(m(10f, 50f, 501f, 801f), m(10f, 50f, -3f, -0.1f), m(10f, 50f, 500f, 800f)));

        assertNull(result.get(0).getPm25());
        assertNull(result.get(0).getPm10());
        assertNull(result.get(1).getPm25());
        assertNull(result.get(1).getPm10());
        assertEquals(500f, result.get(2).getPm25());
        assertEquals(800f, result.get(2).getPm10());
    }

    @Test
    public void missingAndNanValuesAreHandled() {
        List<Measurement> result = service(1000f, 1000f).IncorrectValueFilter(
                List.of(m(10f, null, null, null), m(10f, 50f, Float.NaN, 5f)));

        assertNull(result.get(0).getPm25());
        assertNull(result.get(1).getPm25());
        assertNotNull(result.get(1).getPm10());
    }

    @Test
    public void limitsAreConfigurable() {
        List<Measurement> result = service(10f, 10f).IncorrectValueFilter(List.of(m(10f, 50f, 11f, 11f)));

        assertNull(result.get(0).getPm25());
        assertNull(result.get(0).getPm10());
    }

    @Test
    public void emptyListDoesNotThrow() {
        assertEquals(0, service(1000f, 1000f).IncorrectValueFilter(List.of()).size());
    }
}
