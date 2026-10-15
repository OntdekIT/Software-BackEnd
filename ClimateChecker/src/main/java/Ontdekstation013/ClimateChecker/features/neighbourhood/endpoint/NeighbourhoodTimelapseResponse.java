package Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Antwoord voor een time-lapse over de heatmap: alle tijdstappen in één call.
 *
 * <p>De wijkgrenzen zitten bewust maar één keer in het antwoord, in
 * {@code neighbourhoods}. Per tijdstap volgen alleen nog de gemeten waarden per
 * wijk-id. Zonder die splitsing zou elk frame de volledige polygonen herhalen en
 * werd de payload tientallen keren zo groot.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NeighbourhoodTimelapseResponse {

    /** De wijken met hun grenzen; eenmalig, want die veranderen niet over tijd. */
    @JsonProperty("neighbourhoods")
    private List<TimelapseNeighbourhood> neighbourhoods;

    /** De tijdstappen, chronologisch. */
    @JsonProperty("frames")
    private List<TimelapseFrame> frames;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class TimelapseNeighbourhood {
        @JsonProperty("id")
        private long id;

        @JsonProperty("name")
        private String name;

        /** float[y][x], oftewel float[latitude][longitude] — zelfde vorm als NeighbourhoodDto. */
        @JsonProperty("coordinates")
        private float[][] coordinates;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class TimelapseFrame {
        /** Tijdstip van deze stap, ISO 8601. */
        @JsonProperty("timestamp")
        private String timestamp;

        @JsonProperty("values")
        private List<TimelapseValue> values;

        /** Aantal stations dat in deze stap een bruikbare meting leverde. */
        @JsonProperty("stationCount")
        private int stationCount;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class TimelapseValue {
        @JsonProperty("id")
        private long id;

        /** Null wanneer deze wijk in deze stap geen meting had. */
        @JsonProperty("avgTemp")
        private Float avgTemp;

        @JsonProperty("avgPm25")
        private Float avgPm25;
    }
}
