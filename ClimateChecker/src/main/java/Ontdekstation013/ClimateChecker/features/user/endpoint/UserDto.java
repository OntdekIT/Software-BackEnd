package Ontdekstation013.ClimateChecker.features.user.endpoint;

import Ontdekstation013.ClimateChecker.features.Dto;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class UserDto extends Dto{

    Long id;
    private String firstName;
    private String lastName;
    private String mailAddress;
    private Boolean admin;

    @JsonIgnore
    private String password;



    private Set<StationDto> meetstations = Collections.<StationDto>emptySet();

    public UserDto(){}
    public UserDto(String email){
        mailAddress = email;
    }

    public UserDto(Long id, String firstName, String lastName, String mailAddress, boolean admin, Set<StationDto> meetstations) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mailAddress = mailAddress;
        this.admin = admin;
        this.meetstations = meetstations;
    }
}
