package Ontdekstation013.ClimateChecker.models.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class createStationDto extends Dto{

    public long registrationCode;
    public String databaseTag;
    public long locationId;
}
