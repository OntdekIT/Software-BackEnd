package Ontdekstation013.ClimateChecker.models.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class registerStationDto extends Dto{

    long userId;
    long registerCode;
    String databaseTag;
    String stationName;
    float height;
    String direction;
    boolean isPublic;
    boolean isOutside;
}
