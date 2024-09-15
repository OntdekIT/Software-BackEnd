package Ontdekstation013.ClimateChecker.meetJestad;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.internal.util.Assert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MeetJeStadServiceTest {
    private List<Measurement> measurementList;
    @BeforeEach
    public void init() {
        measurementList = new ArrayList<>();
    }
    @Test
    public void IncorrectValueFilterTest(){
        //Arrange
        measurementList.add(new Measurement(1, Instant.parse("2000-01-01T12:00:00.00Z"), 51.55f, 5f, 33.0f, 200.0f));
        measurementList.add(new Measurement(1, Instant.parse("2000-01-01T12:12:00.00Z"), 51.55f, 5f, 11.0f, 50.0f));
        measurementList.add(new Measurement(1, Instant.parse("2000-01-01T12:24:00.00Z"), 51.55f, 5f, 10.6f, -1.0f));
        measurementList.add(new Measurement(1, Instant.parse("2000-01-01T12:48:00.00Z"), 51.55f, 5f, 10.3f, 50.0f));
        measurementList.add(new Measurement(2, Instant.parse("2000-01-01T12:16:00.00Z"), 51.55f, 5f, 11.1f, 50.0f));
        measurementList.add(new Measurement(2, Instant.parse("2000-01-01T12:20:00.00Z"), 51.55f, 5f, 11.4f, -50.0f));

        MeetJeStadService meetJeStadService = new MeetJeStadService();

        //Act
        List<Measurement> filteredMeasurements = meetJeStadService.IncorrectValueFilter(measurementList);

        //Assert
        Assert.isTrue(filteredMeasurements.size()==measurementList.size());
        Assert.isNull(filteredMeasurements.get(0).getTemperature());
        Assert.isNull(filteredMeasurements.get(0).getHumidity());
        Assert.isNull(filteredMeasurements.get(2).getHumidity());
        Assert.isNull(filteredMeasurements.get(5).getHumidity());
        Assert.notNull(filteredMeasurements.get(1).getTemperature());
        Assert.notNull(filteredMeasurements.get(2).getTemperature());
        Assert.notNull(filteredMeasurements.get(3).getTemperature());
        Assert.notNull(filteredMeasurements.get(4).getTemperature());
        Assert.notNull(filteredMeasurements.get(5).getTemperature());
        Assert.notNull(filteredMeasurements.get(1).getHumidity());
        Assert.notNull(filteredMeasurements.get(3).getHumidity());
        Assert.notNull(filteredMeasurements.get(4).getHumidity());
    }
}
