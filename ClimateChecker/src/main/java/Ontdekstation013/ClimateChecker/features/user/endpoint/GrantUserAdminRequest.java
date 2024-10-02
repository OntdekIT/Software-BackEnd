package Ontdekstation013.ClimateChecker.features.user.endpoint;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GrantUserAdminRequest {
    private String userId;
    private Boolean adminRights;
}
