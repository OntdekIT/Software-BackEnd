package Ontdekstation013.ClimateChecker.neighbourhood;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import Ontdekstation013.ClimateChecker.exception.NotFoundException;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.Neighbourhood;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodCoords;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodRepository;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint.NeighbourhoodDto;
import Ontdekstation013.ClimateChecker.features.station.Station;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class NeighbourhoodByIdTest {

    @Mock
    private MeetJeStadService meetJeStadService;

    @Mock
    private NeighbourhoodRepository neighbourhoodRepository;

    private NeighbourhoodService service;
    private Neighbourhood lars;

    @BeforeEach
    void setUp() {
        service = new NeighbourhoodService(meetJeStadService, neighbourhoodRepository, ZoneOffset.UTC);

        lars = new Neighbourhood(1, "Lars");
        lars.setCoordinates(List.of(
                new NeighbourhoodCoords(1, 51.581110f, 4.987390f, lars),
                new NeighbourhoodCoords(2, 51.583950f, 4.985801f, lars),
                new NeighbourhoodCoords(3, 51.582497f, 4.996515f, lars),
                new NeighbourhoodCoords(4, 51.573829f, 4.993423f, lars),
                new NeighbourhoodCoords(5, 51.574809f, 4.986881f, lars)));
        lenient().when(neighbourhoodRepository.findById(1L)).thenReturn(Optional.of(lars));
    }

    private static Measurement measurement(long stationId, float latitude, float longitude, Float temperature) {
        Measurement measurement = new Measurement(Instant.now(), latitude, longitude, temperature, 50f, 10f, 12f);
        Station station = new Station();
        station.setStationid(stationId);
        measurement.setStation(station);
        return measurement;
    }

    @Test
    void averageTemperatureIsCalculatedFromTheMeasurementsInsideTheNeighbourhood() {
        when(meetJeStadService.getMinuteLimit()).thenReturn(1440);
        when(meetJeStadService.getMeasurements(any(MeetJeStadParameters.class))).thenReturn(List.of(
                measurement(1, 51.578f, 4.99298f, 20.0f),
                measurement(2, 51.578f, 4.99000f, 24.0f),
                // Outside the neighbourhood: must not count.
                measurement(3, 51.700f, 5.10000f, 40.0f)));

        NeighbourhoodDto dto = service.getNeighbourhoodById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Lars");
        assertThat(dto.getCoordinates().length).isEqualTo(5);
        assertThat(dto.getAvgTemp()).isEqualTo(22.0f);
        assertThat(dto.getAvgPm25()).isEqualTo(10f);
    }

    @Test
    void averageTemperatureIsNaNWhenThereAreNoMeasurementsInTheNeighbourhood() {
        when(meetJeStadService.getMinuteLimit()).thenReturn(1440);
        when(meetJeStadService.getMeasurements(any(MeetJeStadParameters.class)))
                .thenReturn(List.of(measurement(3, 51.700f, 5.10000f, 40.0f)));

        NeighbourhoodDto dto = service.getNeighbourhoodById(1L);

        assertThat(dto.getAvgTemp()).isNaN();
        assertThat(dto.getAvgPm25()).isNull();
    }

    @Test
    void neighbourhoodIsStillReturnedWhenTheDataSourceIsUnreachable() {
        when(meetJeStadService.getMinuteLimit()).thenReturn(1440);
        when(meetJeStadService.getMeasurements(any(MeetJeStadParameters.class)))
                .thenThrow(new ResourceAccessException("meetjestad.net onbereikbaar"));

        NeighbourhoodDto dto = service.getNeighbourhoodById(1L);

        assertThat(dto.getName()).isEqualTo("Lars");
        assertThat(dto.getAvgTemp()).isNaN();
    }

    @Test
    void unknownNeighbourhoodIsNotFound() {
        when(neighbourhoodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getNeighbourhoodById(99L)).isInstanceOf(NotFoundException.class);
    }
}
