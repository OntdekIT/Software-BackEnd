package Ontdekstation013.ClimateChecker.models.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class editStationDto extends Dto{

    long Id;
    String name;
    boolean isPublic;
    long registrationCode;
    String databaseTag;
}
