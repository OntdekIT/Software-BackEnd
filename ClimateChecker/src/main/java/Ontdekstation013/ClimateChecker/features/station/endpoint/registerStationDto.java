package Ontdekstation013.ClimateChecker.features.station.endpoint;

import Ontdekstation013.ClimateChecker.features.Dto;
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

    boolean publicInfo;
    boolean outside;
}
