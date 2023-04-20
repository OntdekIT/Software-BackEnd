package Ontdekstation013.ClimateChecker.models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long LocationID;

    @NotBlank
    private float latitude;

    @NotBlank
    private float longitude;
    private float height;

    private String direction;

    private boolean isOutside;

    @OneToOne(mappedBy = "location")
    private Station station;



    public Location(long id, float latitude, float longitude, Station station) {
        this.LocationID = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.station = station;
    }


    public Location(long id, float latitude, float longitude, float height, String direction){
        this.LocationID = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
        this.direction = direction;
    }

    public Location(float latitude, float longitude, float height){
        this.latitude = latitude;
        this.longitude = longitude;
        this.height = height;
    }
}
