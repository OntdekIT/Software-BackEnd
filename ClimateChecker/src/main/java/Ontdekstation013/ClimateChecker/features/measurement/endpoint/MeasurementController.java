package Ontdekstation013.ClimateChecker.features.measurement.endpoint;

import java.time.Instant;
import java.time.format.*;
import java.util.List;
import java.time.*;
import java.time.format.DateTimeFormatter;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

/**
 * For getting measurements of stations, only includes those inside the municipality of Tilburg.
 */
@RestController
@RequestMapping("/api/measurement")
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;

    /**
     * Gets the closest measurement to a given timestamp for each station.
     * Only includes measurements within {@link MeetJeStadService#getMinuteLimit() n} minutes of given timestamps past
     * @param timestamp - ISO 8601 format
     */
    @GetMapping("/history")
    public List<MeasurementDTO> getMeasurementsAtTime(
            @RequestParam(value = "timestamp") String timestamp) {
        try {
            Instant utcDateTime = Instant.parse(timestamp);
            return measurementService.getMeasurementsAtTime(utcDateTime);
        } catch (DateTimeParseException e) {
            throw new InvalidArgumentException("Timestamp must be in ISO 8601 format");
        }
    }

    /**
     * Gets the average, minimum and maximum temperature of a given station for each day in between two given timestamps
     * @param id - stationId
     * @param startDate - dd-MM-yyyy HH:mm format
     * @param endDate -dd-MM-yyyy HH:mm format
     */
    @GetMapping("/history/average/{id}")
    public List<DayMeasurementResponse> getMeasurementsAverage(@PathVariable int id, @RequestParam String startDate, @RequestParam String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm");

        LocalDateTime localDateTimeStart = LocalDateTime.parse(startDate, formatter);
        Instant startInstant = localDateTimeStart.atZone(ZoneId.systemDefault()).toInstant();

        LocalDateTime localDateTimeEnd = LocalDateTime.parse(endDate, formatter);
        Instant endInstant = localDateTimeEnd.atZone(ZoneId.systemDefault()).toInstant();

        if (startInstant.isAfter(endInstant)){
            throw new InvalidArgumentException("Start date is after end date");
        }

        return measurementService.getHistoricalMeasurements(id, startInstant, endInstant);
    }
}
