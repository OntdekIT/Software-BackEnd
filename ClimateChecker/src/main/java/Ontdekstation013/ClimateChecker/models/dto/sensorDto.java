package Ontdekstation013.ClimateChecker.models.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class sensorDto extends Dto{

    int id;
    int stationId;
    int typeId;
    float data;

}
