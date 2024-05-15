package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.features.Dto;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class userDto extends Dto{

    Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String mailAddress;
    private String jwsString;
    private Boolean admin;

    private String password;

    public userDto (){}
    public userDto (String email){
        mailAddress = email;
    }
}
