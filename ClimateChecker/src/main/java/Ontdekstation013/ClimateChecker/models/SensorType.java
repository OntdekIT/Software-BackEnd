package Ontdekstation013.ClimateChecker.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SensorType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private long typeID;
    private String typeName;

    @OneToMany(
            mappedBy = "sensorType",
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<Sensor> sensors = new LinkedList<>();




    public SensorType(long id, String typeName, List<Sensor> sensors){
        this.typeID = id;
        this.typeName = typeName;
        this.sensors = sensors;
    }


    public SensorType(long id, String typeName){
        this.typeID = id;
        this.typeName = typeName;
    }
}
