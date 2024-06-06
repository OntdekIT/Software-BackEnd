package Ontdekstation013.ClimateChecker.features.neighbourhood.endpoint;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

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
    public List<NeighbourhoodDTO> getNeighbourhoodsAtTime(@RequestParam(value = "timestamp") String timestamp) {
        try {
            Instant utcDateTime = Instant.parse(timestamp);
            List<NeighbourhoodDTO> neighbourhoods = neighbourhoodService.getNeighbourhoodsAtTime(utcDateTime);
            return neighbourhoods;
        } catch (DateTimeParseException e) {
            throw new InvalidArgumentException("Timestamp must be in ISO 8601 format");
        }
    }

    /**
     * Gets the average, minimum and maximum temperature of a given neighbourhood for each day in between two given timestamps
     * @param id - neighbourhoodId
     * @param startDate - dd-MM-yyyy HH:mm format
     * @param endDate - dd-MM-yyyy HH:mm format
     */
    @GetMapping("/history/average/{id}")
    public List<DayMeasurementResponse> getNeighbourhoodData(@PathVariable Long id, @RequestParam String startDate, @RequestParam String endDate) {
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

            LocalDateTime localDateTimeStart = LocalDateTime.parse(startDate, formatter);
            Instant startInstant = localDateTimeStart.atZone(ZoneId.systemDefault()).toInstant();

            LocalDateTime localDateTimeEnd = LocalDateTime.parse(endDate, formatter);
            Instant endInstant = localDateTimeEnd.atZone(ZoneId.systemDefault()).toInstant();

            return neighbourhoodService.getHistoricalNeighbourhoodData(id, startInstant, endInstant);
        }
        catch (Exception ex){
            throw ex;
        }

    }

}
