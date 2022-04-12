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
    private long SensorID;

    @ManyToOne
    @JoinColumn(name = "stationID")
    private Station station;

    private int sensorData;

    @ManyToOne
    @JoinColumn(name = "sensorTypeID")
    private SensorType sensorType;



    public Sensor(long id, Station station, int sensorData, SensorType sensorType){
        this.SensorID = id;
        this.station = station;
        this.sensorData = sensorData;
        this.sensorType = sensorType;
    }
}
