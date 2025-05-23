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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    private Instant timestamp;
    private float latitude;
    private float longitude;
    private Float temperature;
    private Float humidity;
    private Float particulate;
    private Boolean is_public;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stationid") // <-- moet verwijzen naar de PK van Station
    private Station station; //

    public Measurement() {
    }

    public Measurement(int stationId, Instant timestamp, float latitude, float longitude, Float temperature, Float humidity, Float particulate) {
        this.station.setStationid((long) stationId);
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.temperature = temperature;
        this.humidity = humidity;
        this.particulate = particulate;
    }
}
