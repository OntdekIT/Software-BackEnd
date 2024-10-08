package Ontdekstation013.ClimateChecker.features.user.authentication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class JwsData {

    private String issuer;
    private String mailAddress;
    private String id;
    private String firstName;
    private String lastName;
    private Date expiration;
}