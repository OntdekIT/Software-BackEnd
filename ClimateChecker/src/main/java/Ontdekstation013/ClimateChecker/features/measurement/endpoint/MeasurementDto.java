package Ontdekstation013.ClimateChecker.features.measurement.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MeasurementDto {

    private Long measurementId;

    @JsonProperty("id")
    @SerializedName("id")
    private int id;

    @JsonProperty("timestamp")
    @SerializedName("timestamp")
    private String timestamp;

    @JsonProperty("latitude")
    @SerializedName("latitude")
    private float latitude;

    @JsonProperty("longitude")
    @SerializedName("longitude")
    private float longitude;

    @JsonProperty("temperature")
    @SerializedName("temperature")
    private Float temperature;

    @JsonProperty("humidity")
    @SerializedName("humidity")
    private Float humidity;

    @JsonProperty("pm2.5")
    @SerializedName("pm2.5")
    private Float pm25;

    @JsonProperty("pm10")
    @SerializedName("pm10")
    private Float pm10;

    @JsonProperty("is_public")
    @SerializedName("is_public")
    private Boolean is_public;

    @JsonProperty("userId")
    private Long userId;

    public Float getParticulate() {
        return pm25 != null ? pm25 : pm10;
    }
}
