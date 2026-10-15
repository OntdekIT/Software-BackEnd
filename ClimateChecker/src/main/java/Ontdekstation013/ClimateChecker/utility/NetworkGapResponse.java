package Ontdekstation013.ClimateChecker.utility;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A period in which most pm-capable stations missed data at the same time, which
 * points to a shared cause rather than a single broken sensor.
 */
@NoArgsConstructor
@Getter
@Setter
public class NetworkGapResponse {
    @JsonProperty("start")
    private String start; // ISO 8601

    @JsonProperty("end")
    private String end; // ISO 8601, exclusive

    @JsonProperty("bucketCount")
    private int bucketCount;

    @JsonProperty("lowestShare")
    private float lowestShare; // lowest share of stations reporting within the gap
}
