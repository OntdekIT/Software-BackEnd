package Ontdekstation013.ClimateChecker.features.authentication.endpoint;

import Ontdekstation013.ClimateChecker.features.Dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class MailDto extends Dto {
    Long id;
    String header;
    String body;
    String footer;
}
