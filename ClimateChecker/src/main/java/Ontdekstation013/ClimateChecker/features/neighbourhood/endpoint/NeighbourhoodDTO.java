package Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NeighbourhoodDTO {
    @JsonProperty("id")
    private long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("coordinates")
    // float[y][x] AKA float[latitude][longitude]
    private float[][] coordinates;
    @JsonProperty("avgTemp")
    private float avgTemp;
}
