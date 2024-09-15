package Ontdekstation013.ClimateChecker.features.authentication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class JwsData {

    private String iss; // ISSUER
    private String mailAddress;
    private String id;
    private String firstName;
    private String lastName;
    private Date exp; // EXPIRATION
}