package Ontdekstation013.ClimateChecker.features.station;

import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StationFilter {
    private String name;
    private String databaseTag;
    private Boolean isPublic;
    private Long registrationCode;
    private String username;
    private List<Long> userIds;
    private Boolean isActive;
}
