package Ontdekstation013.ClimateChecker.utility;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Particulate matter data coverage of the whole network over a period, see {@link DataCoverageLogic}.
 */
@NoArgsConstructor
@Getter
@Setter
public class DataCoverageResponse {
    @JsonProperty("from")
    private String from; // ISO 8601, start of the first bucket

    @JsonProperty("to")
    private String to; // ISO 8601, exclusive

    @JsonProperty("granularity")
    private String granularity; // "hour" or "day"

    @JsonProperty("okThreshold")
    private float okThreshold;

    @JsonProperty("networkGapThreshold")
    private float networkGapThreshold;

    @JsonProperty("buckets")
    private List<String> buckets; // ISO 8601 start of each bucket

    @JsonProperty("stationCount")
    private int stationCount;

    @JsonProperty("pmCapableStationCount")
    private int pmCapableStationCount;

    @JsonProperty("networkPmCoverage")
    private Float networkPmCoverage; // 0..1, null when no station sent pm data

    @JsonProperty("networkTimeline")
    private List<Float> networkTimeline; // share of pm-capable stations reporting, per bucket

    @JsonProperty("networkGaps")
    private List<NetworkGapResponse> networkGaps;

    @JsonProperty("stations")
    private List<StationCoverageResponse> stations;
}
