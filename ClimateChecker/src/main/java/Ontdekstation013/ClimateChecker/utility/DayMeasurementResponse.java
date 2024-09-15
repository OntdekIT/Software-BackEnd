package Ontdekstation013.ClimateChecker.utility;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Used in compiling data into single days
 */
@NoArgsConstructor
@Getter
@Setter
public class DayMeasurementResponse {
    @Setter
    @JsonProperty("timestamp")
    private String timestamp; // Format "dd-MM"
    @JsonProperty("avgTemp")
    private float avgTemp;
    @JsonProperty("minTemp")
    private float minTemp;
    @JsonProperty("maxTemp")
    private float maxTemp;
    @JsonProperty("avgHum")
    private float avgHum;
    @JsonProperty("minHum")
    private float minHum;
    @JsonProperty("maxHum")
    private float maxHum;
}
