package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public record AuthenticationResponse(String token) {
}
