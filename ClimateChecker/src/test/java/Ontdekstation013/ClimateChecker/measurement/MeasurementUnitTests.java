package Ontdekstation013.ClimateChecker.measurement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDto;
import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;

@ExtendWith(MockitoExtension.class)
public class MeasurementUnitTests {
    @InjectMocks
    private MeasurementService measurementService;

    @Mock
    private MeetJeStadService meetJeStadService;
    private List<Measurement> measurementList;

    @BeforeEach
    public void init() {
        measurementList = new ArrayList<>();
    }

    @Test
    public void getMeasurementsAtTime_ClosestToSpecifiedTime() {
        // Arrange
        int minuteMargin = 35;
        when(meetJeStadService.getMinuteLimit()).thenReturn(minuteMargin);

        measurementList.add(new Measurement(1, Instant.parse("2000-01-01T12:12:00.00Z"), 51.55f, 5f, 20.0f, 50.0f));
        measurementList.add(new Measurement(1, Instant.parse("2000-01-01T12:08:00.00Z"), 51.55f, 5f, 20.0f, 50.0f));
        measurementList.add(new Measurement(2, Instant.parse("2000-01-01T12:14:00.00Z"), 51.55f, 5f, 20.0f, 50.0f));
        measurementList.add(new Measurement(2, Instant.parse("2000-01-01T12:16:00.00Z"), 51.55f, 5f, 20.0f, 50.0f));
        when(meetJeStadService.getMeasurements(any(MeetJeStadParameters.class))).thenReturn(measurementList);

        Instant datetime = Instant.parse("2000-01-01T12:16:00.00Z");

        // Act
        List<MeasurementDto> dtos = measurementService.getMeasurementsAtTime(datetime);

        // Assert
        assertEquals(2, dtos.size());

        //IMPORTANT NOTE:
        // I think this formatter in convertToDTO takes daylight savings time into account,
        // meaning the unit tests will only work in regular time if you don't take this into account,
        // therefore the formatter must be used in the unit tests as well.
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("dd-MM-yyyy HH:mm:ss")
                .withZone(ZoneId.of("Europe/Amsterdam"));
        {
            MeasurementDto dto = dtos.stream().filter(obj -> obj.getId() == 1).toList().get(0);
            assertEquals(formatter.format(Instant.parse("2000-01-01T12:12:00.00Z")) , dto.getTimestamp());
        }
        {
            MeasurementDto dto = dtos.stream().filter(obj -> obj.getId() == 2).toList().get(0);
            assertEquals(formatter.format(Instant.parse("2000-01-01T12:16:00.00Z")), dto.getTimestamp());
        }
    }

    @Test
    public void getMeasurementsAverage_OrderAndAverageAndMinAndMax(){
        // Arrange
        int id = 1;
        Instant startDate = Instant.parse("2000-01-01T12:00:00.00Z");
        Instant endDate = Instant.parse("2000-02-01T12:00:00.00Z");

        measurementList.add(new Measurement(1, Instant.parse("2000-01-01T12:00:00.00Z"), 51.55f, 5f, 20.0f, 50.0f));
        measurementList.add(new Measurement(1, Instant.parse("2000-01-02T12:12:00.00Z"), 51.55f, 5f, 15.5f, 50.0f));
        measurementList.add(new Measurement(1, Instant.parse("2000-01-02T12:24:00.00Z"), 51.55f, 5f, 24.3f, 50.0f));
        measurementList.add(new Measurement(1, Instant.parse("2000-01-03T12:48:00.00Z"), 51.55f, 5f, 16.0f, 50.0f));
        measurementList.add(new Measurement(1, Instant.parse("2000-01-03T12:16:00.00Z"), 51.55f, 5f, 16.0f, 50.0f));
        measurementList.add(new Measurement(1, Instant.parse("2000-01-03T12:20:00.00Z"), 51.55f, 5f, 28.0f, 50.0f));
        when(meetJeStadService.getMeasurements(any(MeetJeStadParameters.class))).thenReturn(measurementList);

        Instant datetime = Instant.parse("2000-01-01T12:16:00.00Z");

        // Act
        List<DayMeasurementResponse> responses = measurementService.getHistoricalMeasurements(id, startDate, endDate);

        // Assert
        assertEquals(3, responses.size());
        assertEquals(20f, responses.get(0).getAvgTemp());
        assertEquals(19.9f, responses.get(1).getAvgTemp());
        assertEquals(20f, responses.get(2).getAvgTemp());
        assertEquals(16f, responses.get(2).getMinTemp());
        assertEquals(28f, responses.get(2).getMaxTemp());
    }
}
