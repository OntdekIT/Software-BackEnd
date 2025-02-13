package Ontdekstation013.ClimateChecker.features.measurement;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Measurement {
    private int id;
    private Instant timestamp;
    private float latitude;
    private float longitude;
    private Float temperature;
    private Float humidity;
    private Boolean is_public;

    public Measurement() {
    }

    public Measurement(int id, Instant timestamp, float latitude, float longitude, Float temperature, Float humidity) {
        this.id = id;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.temperature = temperature;
        this.humidity = humidity;
    }
}
