package Ontdekstation013.ClimateChecker.utility;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Particulate matter data coverage of a single station, see {@link DataCoverageLogic}.
 */
@NoArgsConstructor
@Getter
@Setter
public class StationCoverageResponse {
    @JsonProperty("stationId")
    private Long stationId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("stofError")
    private Boolean stofError;

    @JsonProperty("totalReadings")
    private int totalReadings;

    @JsonProperty("pmReadings")
    private int pmReadings;

    @JsonProperty("bucketsWithData")
    private int bucketsWithData;

    @JsonProperty("bucketsWithPm")
    private int bucketsWithPm;

    @JsonProperty("dataCoverage")
    private float dataCoverage; // 0..1, any reading

    @JsonProperty("pmCoverage")
    private float pmCoverage; // 0..1, readings with pm2.5 or pm10

    @JsonProperty("longestPmGapBuckets")
    private int longestPmGapBuckets;

    @JsonProperty("medianPmIntervalMinutes")
    private Float medianPmIntervalMinutes;

    @JsonProperty("status")
    private String status; // OK, IRREGULAR, NO_PM or NO_DATA

    @JsonProperty("pmTimeline")
    private int[] pmTimeline; // number of pm readings per bucket
}
