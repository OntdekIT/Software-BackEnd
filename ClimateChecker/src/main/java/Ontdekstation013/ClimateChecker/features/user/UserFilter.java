package Ontdekstation013.ClimateChecker.features.user;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserFilter {
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isAdmin;
}

