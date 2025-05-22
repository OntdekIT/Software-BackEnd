package Ontdekstation013.ClimateChecker.features.measurement.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MeasurementDto {
    @JsonProperty("id")
    private int id;

    @JsonProperty("stationId")
    private int stationId;

    @JsonProperty("timestamp")  // notation is "dd-mm-yyyy hh:mm:ss" in CET (12-11-2023 06:57:27 voor 7 uur s'ochtends nederlandse tijd)
    private String timestamp;

    @JsonProperty("latitude")
    private float latitude;

    @JsonProperty("longitude")
    private float longitude;

    @JsonProperty("temperature")
    private Float temperature;

    @JsonProperty("humidity")
    private Float humidity;

    @JsonProperty("particulate")
    private Float particulate;

    @JsonProperty("is_public")
    private Boolean is_public;

    //dit kan weg?? weet niet zeker
    @JsonProperty("userId")
    private Long userId;
}
