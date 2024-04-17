package Ontdekstation013.ClimateChecker.features.authentication.endpoint;

import Ontdekstation013.ClimateChecker.features.Dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class registerDto extends Dto{

    String firstName;
    String lastName;
    String mailAddress;
    String password;
    String confirmPassword;
    Long meetstationCode;
}
