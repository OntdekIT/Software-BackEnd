package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint;

import Ontdekstation013.ClimateChecker.features.Dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JwsDto extends Dto {
    String jwsString;
}
