package Ontdekstation013.ClimateChecker.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long SensorID;

    @ManyToOne
    @JoinColumn(name = "stationID")
    private Station station;

    private int sensorData;

    private String sensorType;
}
