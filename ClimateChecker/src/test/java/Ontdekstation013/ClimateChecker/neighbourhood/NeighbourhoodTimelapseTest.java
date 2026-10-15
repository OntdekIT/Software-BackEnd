package Ontdekstation013.ClimateChecker.neighbourhood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.Neighbourhood;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodCoords;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodRepository;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint.NeighbourhoodTimelapseResponse;
import Ontdekstation013.ClimateChecker.features.station.Station;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests voor de time-lapse van de heatmap.
 *
 * <p>De twee eigenschappen die er het meest toe doen: er mag maar één aanroep naar
 * MeetJeStad gebeuren hoeveel stappen je ook vraagt, en een wijk zonder meting in
 * een stap moet null teruggeven in plaats van nul of NaN.
 */
@ExtendWith(MockitoExtension.class)
public class NeighbourhoodTimelapseTest {

    @InjectMocks
    private NeighbourhoodService neighbourhoodService;

    @Mock
    private MeetJeStadService meetJeStadService;

    @Mock
    private NeighbourhoodRepository neighbourhoodRepository;

    private static final int MINUTE_LIMIT = 1440;

    /** Vierkante wijk rond lat 51.50–51.60 / lon 5.00–5.10. */
    private static Neighbourhood squareNeighbourhood(long id, String name, float latLow, float lonLow) {
        Neighbourhood neighbourhood = new Neighbourhood(id, name);
        float latHigh = latLow + 0.10f;
        float lonHigh = lonLow + 0.10f;
        neighbourhood.setCoordinates(List.of(
                new NeighbourhoodCoords(id * 10 + 1, latLow, lonLow, neighbourhood),
                new NeighbourhoodCoords(id * 10 + 2, latHigh, lonLow, neighbourhood),
                new NeighbourhoodCoords(id * 10 + 3, latHigh, lonHigh, neighbourhood),
                new NeighbourhoodCoords(id * 10 + 4, latLow, lonHigh, neighbourhood)
        ));
        return neighbourhood;
    }

    private static Measurement measurement(long stationId, Instant timestamp, float lat, float lon, Float temp) {
        Measurement measurement = new Measurement(timestamp, lat, lon, temp, 50.0f, null, null);
        Station station = new Station();
        station.setStationid(stationId);
        measurement.setStation(station);
        return measurement;
    }

    // --- Validatie ----------------------------------------------------------

    @Test
    public void tooFewStepsIsRejected() {
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Instant from = to.minus(Duration.ofHours(24));

        assertThrows(InvalidArgumentException.class,
                () -> neighbourhoodService.getNeighbourhoodTimelapse(from, to, 1));
    }

    @Test
    public void tooManyStepsIsRejected() {
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Instant from = to.minus(Duration.ofHours(24));

        assertThrows(InvalidArgumentException.class,
                () -> neighbourhoodService.getNeighbourhoodTimelapse(from, to, 500));
    }

    @Test
    public void fromAfterToIsRejected() {
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Instant from = to.plus(Duration.ofHours(1));

        assertThrows(InvalidArgumentException.class,
                () -> neighbourhoodService.getNeighbourhoodTimelapse(from, to, 12));
    }

    @Test
    public void equalFromAndToIsRejected() {
        Instant moment = Instant.parse("2026-01-02T00:00:00Z");

        assertThrows(InvalidArgumentException.class,
                () -> neighbourhoodService.getNeighbourhoodTimelapse(moment, moment, 12));
    }

    // --- Frames -------------------------------------------------------------

    @Test
    public void framesAreEvenlySpacedAndCoverTheWholeRange() {
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Instant from = to.minus(Duration.ofHours(24));

        when(meetJeStadService.getMinuteLimit()).thenReturn(MINUTE_LIMIT);
        when(neighbourhoodRepository.findAll()).thenReturn(List.of(squareNeighbourhood(1L, "Centrum", 51.50f, 5.00f)));
        when(meetJeStadService.getMeasurements(any())).thenReturn(new ArrayList<>());

        NeighbourhoodTimelapseResponse response = neighbourhoodService.getNeighbourhoodTimelapse(from, to, 5);

        assertEquals(5, response.getFrames().size());
        // Eerste stap valt op 'from', laatste precies op 'to'.
        assertEquals(from.toString(), response.getFrames().get(0).getTimestamp());
        assertEquals(to.toString(), response.getFrames().get(4).getTimestamp());
        // 24 uur over 5 stappen betekent 6 uur per stap.
        assertEquals(from.plus(Duration.ofHours(6)).toString(), response.getFrames().get(1).getTimestamp());
        assertEquals(from.plus(Duration.ofHours(12)).toString(), response.getFrames().get(2).getTimestamp());
    }

    @Test
    public void measurementsAreFetchedOnceRegardlessOfStepCount() {
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Instant from = to.minus(Duration.ofHours(24));

        when(meetJeStadService.getMinuteLimit()).thenReturn(MINUTE_LIMIT);
        when(neighbourhoodRepository.findAll()).thenReturn(List.of(squareNeighbourhood(1L, "Centrum", 51.50f, 5.00f)));
        when(meetJeStadService.getMeasurements(any())).thenReturn(new ArrayList<>());

        neighbourhoodService.getNeighbourhoodTimelapse(from, to, 48);

        // Dit is de hele reden dat dit endpoint bestaat: 48 stappen, één aanroep.
        verify(meetJeStadService, times(1)).getMeasurements(any());
    }

    @Test
    public void fetchedRangeIncludesTheWindowBeforeTheFirstStep() {
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Instant from = to.minus(Duration.ofHours(24));

        when(meetJeStadService.getMinuteLimit()).thenReturn(MINUTE_LIMIT);
        when(neighbourhoodRepository.findAll()).thenReturn(List.of(squareNeighbourhood(1L, "Centrum", 51.50f, 5.00f)));
        ArgumentCaptor<MeetJeStadParameters> captor = ArgumentCaptor.forClass(MeetJeStadParameters.class);
        when(meetJeStadService.getMeasurements(captor.capture())).thenReturn(new ArrayList<>());

        neighbourhoodService.getNeighbourhoodTimelapse(from, to, 4);

        // Zonder die marge zou de eerste stap nooit een meting kunnen vinden.
        assertEquals(from.minus(Duration.ofMinutes(MINUTE_LIMIT)), captor.getValue().StartDate);
        assertEquals(to, captor.getValue().EndDate);
    }

    @Test
    public void neighbourhoodBoundariesAreReturnedOnce() {
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Instant from = to.minus(Duration.ofHours(24));

        when(meetJeStadService.getMinuteLimit()).thenReturn(MINUTE_LIMIT);
        when(neighbourhoodRepository.findAll()).thenReturn(List.of(
                squareNeighbourhood(1L, "Centrum", 51.50f, 5.00f),
                squareNeighbourhood(2L, "Noord", 51.20f, 5.00f)));
        when(meetJeStadService.getMeasurements(any())).thenReturn(new ArrayList<>());

        NeighbourhoodTimelapseResponse response = neighbourhoodService.getNeighbourhoodTimelapse(from, to, 6);

        assertEquals(2, response.getNeighbourhoods().size());
        assertEquals("Centrum", response.getNeighbourhoods().get(0).getName());
        assertNotNull(response.getNeighbourhoods().get(0).getCoordinates());
        // Elk frame heeft wel een waarde per wijk, maar geen coordinaten.
        response.getFrames().forEach(frame -> assertEquals(2, frame.getValues().size()));
    }

    // --- Waarden per wijk ---------------------------------------------------

    @Test
    public void neighbourhoodWithoutMeasurementsGetsNullInsteadOfZero() {
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Instant from = to.minus(Duration.ofHours(24));

        when(meetJeStadService.getMinuteLimit()).thenReturn(MINUTE_LIMIT);
        when(neighbourhoodRepository.findAll()).thenReturn(List.of(
                squareNeighbourhood(1L, "Centrum", 51.50f, 5.00f),
                squareNeighbourhood(2L, "Noord", 51.20f, 5.00f)));
        // Meting ligt in Centrum, niet in Noord.
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of(
                measurement(1L, to.minus(Duration.ofHours(1)), 51.55f, 5.05f, 20.0f)));

        NeighbourhoodTimelapseResponse response = neighbourhoodService.getNeighbourhoodTimelapse(from, to, 2);

        NeighbourhoodTimelapseResponse.TimelapseFrame lastFrame = response.getFrames().get(1);
        NeighbourhoodTimelapseResponse.TimelapseValue centrum = lastFrame.getValues().stream()
                .filter(v -> v.getId() == 1L).findFirst().orElseThrow();
        NeighbourhoodTimelapseResponse.TimelapseValue noord = lastFrame.getValues().stream()
                .filter(v -> v.getId() == 2L).findFirst().orElseThrow();

        assertEquals(20.0f, centrum.getAvgTemp());
        // Null, zodat de kaart deze wijk grijs laat in plaats van ijskoud te kleuren.
        assertNull(noord.getAvgTemp());
    }

    @Test
    public void aMeasurementDoesNotLeakIntoAnEarlierFrame() {
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Instant from = to.minus(Duration.ofHours(24));

        when(meetJeStadService.getMinuteLimit()).thenReturn(MINUTE_LIMIT);
        when(neighbourhoodRepository.findAll()).thenReturn(List.of(squareNeighbourhood(1L, "Centrum", 51.50f, 5.00f)));
        // Meting valt ná de eerste stap.
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of(
                measurement(1L, to.minus(Duration.ofMinutes(30)), 51.55f, 5.05f, 25.0f)));

        NeighbourhoodTimelapseResponse response = neighbourhoodService.getNeighbourhoodTimelapse(from, to, 3);

        assertNull(response.getFrames().get(0).getValues().get(0).getAvgTemp());
        assertEquals(25.0f, response.getFrames().get(2).getValues().get(0).getAvgTemp());
    }

    @Test
    public void frameReportsHowManyStationsContributed() {
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Instant from = to.minus(Duration.ofHours(24));

        when(meetJeStadService.getMinuteLimit()).thenReturn(MINUTE_LIMIT);
        when(neighbourhoodRepository.findAll()).thenReturn(List.of(squareNeighbourhood(1L, "Centrum", 51.50f, 5.00f)));
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of(
                measurement(1L, to.minus(Duration.ofMinutes(10)), 51.55f, 5.05f, 20.0f),
                measurement(2L, to.minus(Duration.ofMinutes(20)), 51.56f, 5.06f, 22.0f),
                // Tweede meting van station 1: telt niet dubbel, alleen de dichtstbijzijnde.
                measurement(1L, to.minus(Duration.ofMinutes(90)), 51.55f, 5.05f, 18.0f)));

        NeighbourhoodTimelapseResponse response = neighbourhoodService.getNeighbourhoodTimelapse(from, to, 2);

        assertEquals(2, response.getFrames().get(1).getStationCount());
        // Gemiddelde van 20 en 22, dus niet van 18.
        assertEquals(21.0f, response.getFrames().get(1).getValues().get(0).getAvgTemp());
    }

    @Test
    public void measurementOutsideEveryNeighbourhoodIsIgnored() {
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Instant from = to.minus(Duration.ofHours(24));

        when(meetJeStadService.getMinuteLimit()).thenReturn(MINUTE_LIMIT);
        when(neighbourhoodRepository.findAll()).thenReturn(List.of(squareNeighbourhood(1L, "Centrum", 51.50f, 5.00f)));
        when(meetJeStadService.getMeasurements(any())).thenReturn(List.of(
                measurement(1L, to.minus(Duration.ofMinutes(10)), 52.90f, 6.90f, 30.0f)));

        NeighbourhoodTimelapseResponse response = neighbourhoodService.getNeighbourhoodTimelapse(from, to, 2);

        assertNull(response.getFrames().get(1).getValues().get(0).getAvgTemp());
        // De meting telt wel mee als station: hij is er, hij ligt alleen in geen enkele wijk.
        assertTrue(response.getFrames().get(1).getStationCount() >= 1);
    }
}
