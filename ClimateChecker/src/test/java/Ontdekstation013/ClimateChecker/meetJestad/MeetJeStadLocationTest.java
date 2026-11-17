package Ontdekstation013.ClimateChecker.meetJestad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

public class MeetJeStadLocationTest {
    private static final float TILBURG_LAT = 51.56f;
    private static final float TILBURG_LON = 5.08f;

    private StationRepository stationRepository;
    private RestTemplate restTemplate;
    private MeetJeStadService service;

    @BeforeEach
    public void setUp() {
        stationRepository = mock(StationRepository.class);
        restTemplate = mock(RestTemplate.class);
        service = new MeetJeStadService(stationRepository, "http://localhost/?x=1",
                0f, 100f, 0f, 1000f, 0f, 1000f);
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
    }

    private static String row(int id, String timestamp, String lat, String lon) {
        StringBuilder json = new StringBuilder("{\"id\":" + id + ",\"timestamp\":\"" + timestamp + "\",\"temperature\":15.0");
        if (lat != null) {
            json.append(",\"latitude\":").append(lat);
        }
        if (lon != null) {
            json.append(",\"longitude\":").append(lon);
        }
        return json.append("}").toString();
    }

    private List<Measurement> fetch(String... rows) {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("[" + String.join(",", rows) + "]"));
        MeetJeStadParameters params = new MeetJeStadParameters();
        params.includeFaultyMeasurements = true;
        return service.getMeasurements(params);
    }

    private Station stationWithLastKnown(long id, Float lat, Float lon) {
        Station station = new Station();
        station.setStationid(id);
        station.setLastKnownLatitude(lat);
        station.setLastKnownLongitude(lon);
        when(stationRepository.getMeetstationByStationid(id)).thenReturn(station);
        return station;
    }

    @Test
    public void measurementWithoutLocationGetsLastKnownLocation() {
        stationWithLastKnown(1L, TILBURG_LAT, TILBURG_LON);

        List<Measurement> result = fetch(row(1, "2026-01-01 12:00:00", null, null));

        assertEquals(1, result.size());
        assertTrue(result.get(0).isLocationInherited());
        assertEquals(TILBURG_LAT, result.get(0).getLatitude());
        assertEquals(TILBURG_LON, result.get(0).getLongitude());
    }

    @Test
    public void measurementAtZeroZeroGetsLastKnownLocation() {
        stationWithLastKnown(1L, TILBURG_LAT, TILBURG_LON);

        List<Measurement> result = fetch(row(1, "2026-01-01 12:00:00", "0.0", "0.0"));

        assertEquals(1, result.size());
        assertTrue(result.get(0).isLocationInherited());
        assertEquals(TILBURG_LAT, result.get(0).getLatitude());
    }

    @Test
    public void measurementWithNanLocationGetsLastKnownLocation() {
        stationWithLastKnown(1L, TILBURG_LAT, TILBURG_LON);

        List<Measurement> result = fetch(row(1, "2026-01-01 12:00:00", "\"NaN\"", "\"NaN\""));

        assertEquals(1, result.size());
        assertTrue(result.get(0).isLocationInherited());
        assertEquals(TILBURG_LON, result.get(0).getLongitude());
    }

    @Test
    public void measurementOutsideTilburgIsStillDiscarded() {
        stationWithLastKnown(1L, TILBURG_LAT, TILBURG_LON);

        List<Measurement> result = fetch(row(1, "2026-01-01 12:00:00", "52.37", "4.90"));

        assertTrue(result.isEmpty());
    }

    @Test
    public void measurementWithoutLocationAndWithoutKnownStationLocationIsDiscarded() {
        stationWithLastKnown(1L, null, null);

        List<Measurement> result = fetch(row(1, "2026-01-01 12:00:00", null, null));

        assertTrue(result.isEmpty());
    }

    @Test
    public void measurementWithValidLocationIsNotMarkedAsInheritedAndUpdatesStation() {
        Station station = stationWithLastKnown(1L, null, null);

        List<Measurement> result = fetch(row(1, "2026-01-01 12:00:00", "51.56", "5.08"));

        assertEquals(1, result.size());
        assertFalse(result.get(0).isLocationInherited());
        assertEquals(TILBURG_LAT, station.getLastKnownLatitude());
        verify(stationRepository).save(station);
    }

    @Test
    public void missingLocationUsesNewestValidLocationFromSameBatch() {
        stationWithLastKnown(1L, 51.60f, 5.10f);

        List<Measurement> result = fetch(
                row(1, "2026-01-01 12:00:00", "51.56", "5.08"),
                row(1, "2026-01-01 12:10:00", null, null));

        assertEquals(2, result.size());
        Measurement inherited = result.stream().filter(Measurement::isLocationInherited).findFirst().orElseThrow();
        assertEquals(TILBURG_LAT, inherited.getLatitude());
    }

    @Test
    public void unchangedLastKnownLocationIsNotSavedAgain() {
        Station station = stationWithLastKnown(1L, TILBURG_LAT, TILBURG_LON);

        fetch(row(1, "2026-01-01 12:00:00", "51.56", "5.08"));

        verify(stationRepository, never()).save(station);
    }

    @Test
    public void unknownStationWithoutLocationIsDiscarded() {
        when(stationRepository.getMeetstationByStationid(2L)).thenReturn(null);

        List<Measurement> result = fetch(row(2, "2026-01-01 12:00:00", null, null));

        assertTrue(result.isEmpty());
    }
}
