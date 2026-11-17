package Ontdekstation013.ClimateChecker.snapshot;

import Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint.NeighbourhoodDto;
import Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint.NeighbourhoodTimelapseResponse;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Onveranderlijke set vooraf berekende antwoorden, in een keer verwisseld na elke ingest-run. Alle
 * endpoints die hieruit lezen zien daardoor altijd een onderling consistente staat.
 *
 * @param stations       antwoord van /stationsMetMeasurements voor "nu"
 * @param neighbourhoods antwoord van /neighbourhood/history voor "nu"
 * @param timelapses     time-lapse over de laatste 24 uur, per aantal stappen
 * @param overviewJson   het antwoord van /api/snapshot, al als JSON
 */
public record MapSnapshot(long version, Instant builtAt, Instant dataAsOf, List<StationDto> stations,
                          List<NeighbourhoodDto> neighbourhoods,
                          Map<Integer, NeighbourhoodTimelapseResponse> timelapses,
                          Overview overview, byte[] overviewJson, String etag) {
}
