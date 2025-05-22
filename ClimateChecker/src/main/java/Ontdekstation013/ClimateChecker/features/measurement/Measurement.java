package Ontdekstation013.ClimateChecker.features.measurement;

import Ontdekstation013.ClimateChecker.features.station.Station;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class Measurement {
    private Instant timestamp;
    private float latitude;
    private float longitude;
    private Float temperature;
    private Float humidity;
    private Float particulate;
    private Boolean is_public;
    @Id
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stationid") // <-- moet verwijzen naar de PK van Station
    private Station station; //

    public Measurement() {
    }

    public Measurement(int id, Instant timestamp, float latitude, float longitude, Float temperature, Float humidity, Float particulate) {
        this.id = id;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.temperature = temperature;
        this.humidity = humidity;
        this.particulate = particulate;
    }
}
