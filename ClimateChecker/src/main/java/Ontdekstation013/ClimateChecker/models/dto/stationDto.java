package Ontdekstation013.ClimateChecker.models.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class stationDto extends Dto{

    int id;
    String name;
    float height;
    int locationId;
    String locationName;
    float latitude;
    float longitude;
    List<sensorDto> sensors;
}
