package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.features.Dto;
import Ontdekstation013.ClimateChecker.features.meetstation.endpoint.MeetstationDto;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class userDto extends Dto{

    Long id;
    private String firstName;
    private String lastName;
    private String mailAddress;
    private String jwsString;
    private Boolean admin;

    private String password;

    private Set<MeetstationDto> meetstations = Collections.<MeetstationDto>emptySet();

    public userDto (){}
    public userDto (String email){
        mailAddress = email;
    }

    public userDto(Long id, String firstName, String lastName, String mailAddress, boolean admin, Set<MeetstationDto> meetstations) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mailAddress = mailAddress;
        this.admin = admin;
        this.meetstations = meetstations;
    }
}
