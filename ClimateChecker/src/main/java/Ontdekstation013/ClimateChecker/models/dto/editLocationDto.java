package Ontdekstation013.ClimateChecker.models.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class editLocationDto extends Dto {
    public long id;
    public String direction;
    public float height;
    public boolean isOutside;
}
