package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.features.Dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDataDto extends Dto{

    Long id;
    String firstName;
    String lastName;
    String userName;
    String mailAddress;
    boolean admin;

    @JsonIgnore
    String password;




    Boolean isStationActive;

}
