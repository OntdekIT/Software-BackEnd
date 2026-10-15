package Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodService;
import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * For getting measurements of stations divided into neighbourhoods, only includes those inside the municipality of Tilburg.
 */
@RestController
@RequestMapping("/api/neighbourhood")
@RequiredArgsConstructor
public class NeighbourhoodController {

    private final NeighbourhoodService neighbourhoodService;

    /**
     * Gets all neighbourhoods at a given timestamp.
     * Only includes measurements within {@link MeetJeStadService#getMinuteLimit() n} minutes of given timestamps past
     * @param timestamp - ISO 8601 format
     */
    @GetMapping("/history")
    public List<NeighbourhoodDto> getNeighbourhoodsAtTime(@RequestParam(value = "timestamp") String timestamp) {
        try {
            Instant utcDateTime = Instant.parse(timestamp);
            return neighbourhoodService.getNeighbourhoodsAtTime(utcDateTime);
        } catch (DateTimeParseException e) {
            throw new InvalidArgumentException("Timestamp must be in ISO 8601 format");
        }
    }

    /**
     * Geeft een time-lapse van de heatmap: per tijdstap de gemiddelde temperatuur
     * en PM2.5 per wijk, in één antwoord.
     *
     * <p>Bedoeld om de kaart af te spelen als tijdreeks. Het alternatief —
     * {@code /history} per tijdstip aanroepen — haalt elke keer het volledige
     * meetvenster op bij MeetJeStad, wat voor een reeks van tientallen stappen
     * onnodig traag is.
     *
     * @param from  ISO 8601, tijdstip van de eerste stap (standaard 24 uur geleden)
     * @param to    ISO 8601, tijdstip van de laatste stap (standaard nu)
     * @param steps aantal stappen inclusief begin en eind (standaard 24)
     */
    @GetMapping("/history/timelapse")
    public NeighbourhoodTimelapseResponse getNeighbourhoodTimelapse(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "24") int steps) {

        Instant toInstant = (to != null) ? parseIso8601(to) : Instant.now();
        Instant fromInstant = (from != null) ? parseIso8601(from) : toInstant.minus(Duration.ofHours(24));

        return neighbourhoodService.getNeighbourhoodTimelapse(fromInstant, toInstant, steps);
    }

    private Instant parseIso8601(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new InvalidArgumentException("Date must be in ISO 8601 format (e.g. 2026-05-10T14:00:00Z)");
        }
    }

    /**
     * Gets the average, minimum and maximum temperature of a given neighbourhood for each day in between two given timestamps
     * @param id - neighbourhoodId
     * @param startDate - dd-MM-yyyy HH:mm format
     * @param endDate - dd-MM-yyyy HH:mm format
     */
    @GetMapping("/history/average/{id}")
    public List<DayMeasurementResponse> getNeighbourhoodData(
            @PathVariable Long id,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

            LocalDateTime localDateTimeStart = LocalDateTime.parse(startDate, formatter);
            Instant startInstant = localDateTimeStart.atZone(ZoneId.systemDefault()).toInstant();

            LocalDateTime localDateTimeEnd = LocalDateTime.parse(endDate, formatter);
            Instant endInstant = localDateTimeEnd.atZone(ZoneId.systemDefault()).toInstant();

            return neighbourhoodService.getHistoricalNeighbourhoodData(id, startInstant, endInstant);
        } catch (DateTimeParseException e) {
            throw new InvalidArgumentException("Date must be in format dd-MM-yyyy HH:mm");
        }
    }

    @GetMapping("/{id}")
    public NeighbourhoodDto getNeighbourhoodById(@PathVariable Long id) {
        return neighbourhoodService.getNeighbourhoodById(id);
    }
}