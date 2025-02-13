package Ontdekstation013.ClimateChecker.neighbourhood;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.Neighbourhood;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodCoords;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodRepository;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodService;
import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NeighbourhoodUnitTest {
    @InjectMocks
    private NeighbourhoodService neighbourhoodService;

    @Mock
    private MeetJeStadService meetJeStadService;
    private List<Measurement> measurementList;

    @Mock
    private NeighbourhoodRepository NeighbourhoodRepository;
    private List<Neighbourhood> neighbourhoodList;

    @BeforeEach
    public void init() {
        neighbourhoodList = new ArrayList<>();
        measurementList = new ArrayList<>();

        Neighbourhood lars = new Neighbourhood(1, "Lars");
        lars.setCoordinates(List.of(
                new NeighbourhoodCoords(1, 51.581110f, 4.987390f, lars),
                new NeighbourhoodCoords(2, 51.583950f, 4.985801f, lars),
                new NeighbourhoodCoords(3, 51.582497f, 4.996515f, lars),
                new NeighbourhoodCoords(4, 51.573829f, 4.993423f, lars),
                new NeighbourhoodCoords(5, 51.574809f, 4.986881f, lars)
        ));
        neighbourhoodList.add(lars);
        measurementList.add(new Measurement(1, Instant.now(), 51.578000f, 4.992980f, 22.0f, 50.0f));
        measurementList.add(new Measurement(2, Instant.now(), 51.578000f, 40.992980f, 23.0f, 50.0f));

        Neighbourhood niels = new Neighbourhood(2, "Niels");
        niels.setCoordinates(List.of(
                new NeighbourhoodCoords(6, 51.603059f, 4.967818f, niels),
                new NeighbourhoodCoords(7, 51.601819f, 4.968806f, niels),
                new NeighbourhoodCoords(8, 51.598061f, 4.967582f, niels),
                new NeighbourhoodCoords(9, 51.597208f, 4.975675f, niels),
                new NeighbourhoodCoords(10, 51.596235f, 4.977636f, niels),
                new NeighbourhoodCoords(11, 51.594022f, 4.977936f, niels),
                new NeighbourhoodCoords(12, 51.611448f, 4.948795f, niels),
                new NeighbourhoodCoords(13, 51.612307f, 4.949632f, niels),
                new NeighbourhoodCoords(14, 51.591109f, 5.005992f, niels),
                new NeighbourhoodCoords(15, 51.588150f, 5.005409f, niels),
                new NeighbourhoodCoords(16, 51.585917f, 5.009339f, niels),
                new NeighbourhoodCoords(17, 51.584410f, 5.007150f, niels),
                new NeighbourhoodCoords(18, 51.585917f, 4.997504f, niels),
                new NeighbourhoodCoords(19, 51.582597f, 4.996380f, niels),
                new NeighbourhoodCoords(20, 51.583977f, 4.985841f, niels)
        ));
        neighbourhoodList.add(niels);
        measurementList.add(new Measurement(3, Instant.now(), 51.5869f, 4.999210f, 23.0f, 50.0f));
        measurementList.add(new Measurement(4, Instant.now(), 51.5869f, 4.999210f, 22.0f, 50.0f));
    }

    @Test
    public void getHistoricalNeighbourhoodData_Algorithm() {
        // Arrange
        Instant startDate = Instant.parse("2000-01-01T20:00:00.00Z");
        Instant endDate = Instant.parse("2000-01-10T20:00:00.00Z");
        Neighbourhood stijn = new Neighbourhood(1, "Stijn");

        stijn.setCoordinates(List.of(
                new NeighbourhoodCoords(1, 51.5605f, 5.06465f, stijn),
                new NeighbourhoodCoords(2, 51.5563f, 5.06387f, stijn),
                new NeighbourhoodCoords(3, 51.5573f, 5.04964f, stijn),
                new NeighbourhoodCoords(4, 51.5616f, 5.05054f, stijn)
        ));

        List<Measurement> lastDayMeasurements = List.of(
                new Measurement(1, endDate.minus(Duration.ofHours(16)), 51.5587f, 5.0567f, 28.0f, 50.0f),
                new Measurement(2, endDate.minus(Duration.ofHours(10)), 51.5587f, 5.0567f, 18.0f, 50.0f),
                new Measurement(3, endDate.minus(Duration.ofHours(4)), 51.5587f, 5.0567f, 20.0f, 50.0f)
        );
        List<Measurement> allMeasurements = List.of(
                new Measurement(1, endDate.minus(Duration.ofHours(40)), 51.5587f, 5.0567f, 28.0f, 50.0f),
                new Measurement(2, endDate.minus(Duration.ofHours(34)), 51.5587f, 5.0567f, 18.0f, 50.0f),
                new Measurement(3, endDate.minus(Duration.ofHours(28)), 51.5587f, 5.0567f, 20.0f, 50.0f),
                lastDayMeasurements.get(0),
                lastDayMeasurements.get(1),
                lastDayMeasurements.get(2)
        );

        when(NeighbourhoodRepository.findById(1L)).thenReturn(Optional.of(stijn));

        ArgumentCaptor<MeetJeStadParameters> paramCaptor = ArgumentCaptor.forClass(MeetJeStadParameters.class);
        when(meetJeStadService.getMeasurements(paramCaptor.capture()))
                .thenReturn(lastDayMeasurements, allMeasurements);

        // Act
        List<DayMeasurementResponse> dayMeasurements = neighbourhoodService.getHistoricalNeighbourhoodData(stijn.getId(), startDate, endDate);

        // Assert
        verify(meetJeStadService, times(2)).getMeasurements(any());
        verify(NeighbourhoodRepository).findById(1L);

        assertEquals(2, dayMeasurements.size());

        DayMeasurementResponse firstResponse = dayMeasurements.get(0);
        assertEquals("09-01", firstResponse.getTimestamp());
        assertEquals(22, firstResponse.getAvgTemp());
        assertEquals(18, firstResponse.getMinTemp());
        assertEquals(28, firstResponse.getMaxTemp());

        DayMeasurementResponse secondResponse = dayMeasurements.get(1);
        assertEquals("10-01", secondResponse.getTimestamp());
        assertEquals(22, secondResponse.getAvgTemp());
        assertEquals(18, secondResponse.getMinTemp());
        assertEquals(28, secondResponse.getMaxTemp());

        MeetJeStadParameters firstCall = paramCaptor.getAllValues().get(0);
        assertEquals(endDate.minus(Duration.ofHours(1)), firstCall.StartDate);
        assertEquals(endDate, firstCall.EndDate);

        MeetJeStadParameters secondCall = paramCaptor.getAllValues().get(1);
        assertEquals(startDate, secondCall.StartDate);
        assertEquals(endDate, secondCall.EndDate);
        List<Integer> stationIds = List.of(1, 2, 3);
        assertEquals(stationIds, secondCall.StationIds);
    }
}