package Ontdekstation013.ClimateChecker.models.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class stationDto extends Dto{

    long id;
    String name;
    float height;
    String direction;
    long locationId;
    String locationName;
    float latitude;
    float longitude;
    boolean ispublic;
    boolean isoutside;
    List<sensorDto> sensors;
}

