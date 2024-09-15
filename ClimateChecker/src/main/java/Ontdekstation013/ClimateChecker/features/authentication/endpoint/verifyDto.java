package Ontdekstation013.ClimateChecker.features.authentication.endpoint;

import Ontdekstation013.ClimateChecker.features.Dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class verifyDto extends Dto{
    String mailAddress;
    String code;
}
