package Ontdekstation013.ClimateChecker.models.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class userDto extends Dto{

    Long id;
    String username;
    String mailAddress;
    String passwordHash;
    String passwordSalt;
}
