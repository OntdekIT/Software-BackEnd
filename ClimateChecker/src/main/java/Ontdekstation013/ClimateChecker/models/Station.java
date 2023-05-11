package Ontdekstation013.ClimateChecker.models;

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
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long stationID;

    @OneToMany(
            mappedBy = "station",
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<Sensor> sensors = new LinkedList<>();

    @ManyToOne
    @JoinColumn(name = "userID")
    private User owner;


    private long registrationCode;

    // In the case of multiple data sources, a tag is added to the registrationcode. in the case of "MeetJeStad" data, this is MJS[registrationcode]
    private String databaseTag;

    @OneToOne(cascade = CascadeType.MERGE)
    private Location location;

    private String Name;

    private boolean isPublic;


    public Station(long id, List<Sensor> sensors, User owner, Location location, String name, boolean isPublic) {
        this.stationID = id;
        this.sensors = sensors;
        this.owner = owner;
        this.location = location;
        this.Name = name;
        this.isPublic = isPublic;
    }

    public Station(User owner, String name, Location location, boolean isPublic) {
        this.owner = owner;
        this.location = location;
        this.Name = name;
        this.isPublic = isPublic;
    }

    public Station(long id, String name, Location location, User owner, boolean isPublic){
        this.stationID = id;
        this.Name = name;
        this.location = location;
        this.owner = owner;
        this.isPublic = isPublic;
    }

    public Station(long id, long registrationCode, String name, Location location, User owner, boolean isPublic){
        this.stationID = id;
        this.registrationCode = registrationCode;
        this.Name = name;
        this.location = location;
        this.owner = owner;
        this.isPublic = isPublic;
    }


    public Station(long id, long registrationCode,String databaseTag, String name, Location location, User owner, boolean isPublic){
        this.stationID = id;
        this.registrationCode = registrationCode;
        this.databaseTag = databaseTag;
        this.Name = name;
        this.location = location;
        this.owner = owner;
        this.isPublic = isPublic;
    }


}
