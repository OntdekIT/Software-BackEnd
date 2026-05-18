package Ontdekstation013.ClimateChecker.utility;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RegionAverageBucketResponse {
    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("min")
    private float min;

    @JsonProperty("max")
    private float max;

    @JsonProperty("avg")
    private float avg;

    @JsonProperty("pm25")
    private Float pm25;

    @JsonProperty("pm10")
    private Float pm10;
}