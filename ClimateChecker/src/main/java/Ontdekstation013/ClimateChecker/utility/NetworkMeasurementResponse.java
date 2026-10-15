package Ontdekstation013.ClimateChecker.utility;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Netwerkbrede statistiek per dagbucket: het minimum, het maximum, de mediaan
 * en het gemiddelde over alle meetstations samen.
 *
 * <p>De drie waarden worden over dezelfde verzameling metingen berekend, zodat
 * de panelen in de frontend onderling vergelijkbaar zijn: {@code min} is de
 * koudste meting van die dag ergens in het netwerk, {@code max} de warmste en
 * {@code median} de middelste meting. Het gaat dus om de mediaan van alle
 * metingen, niet om de mediaan van de stationsgemiddelden.
 *
 * <p>{@code timestamp} gebruikt bewust hetzelfde "dd-MM"-formaat als
 * {@link DayMeasurementResponse}, zodat de x-as van de netwerkgrafieken
 * gelijkloopt met die van het geselecteerde station.
 */
@NoArgsConstructor
@Getter
@Setter
public class NetworkMeasurementResponse {

    @JsonProperty("timestamp")
    private String timestamp; // Format "dd-MM"

    /** Laagste gemeten temperatuur binnen het netwerk in deze bucket. */
    @JsonProperty("minTemp")
    private Float minTemp;

    /** Hoogste gemeten temperatuur binnen het netwerk in deze bucket. */
    @JsonProperty("maxTemp")
    private Float maxTemp;

    /** Mediaan van alle gemeten temperaturen binnen het netwerk in deze bucket. */
    @JsonProperty("medianTemp")
    private Float medianTemp;

    /** Gemiddelde van alle gemeten temperaturen binnen het netwerk in deze bucket. */
    @JsonProperty("avgTemp")
    private Float avgTemp;

    /** Aantal unieke stations dat aan deze bucket heeft bijgedragen. */
    @JsonProperty("stationCount")
    private int stationCount;

    /** Aantal metingen waarop de statistiek is gebaseerd. */
    @JsonProperty("measurementCount")
    private int measurementCount;
}
