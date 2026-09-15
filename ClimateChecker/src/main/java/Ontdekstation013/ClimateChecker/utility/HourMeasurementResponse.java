package Ontdekstation013.ClimateChecker.utility;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Used in compiling data into single hours for a day
 */
@NoArgsConstructor
@Getter
@Setter
public class HourMeasurementResponse {
    @JsonProperty("timestamp")
    private String timestamp; // Format "HH:mm"

    @JsonProperty("avgTemp")
    private float avgTemp;

    @JsonProperty("avgPm25")
    private float avgPm25;
}
