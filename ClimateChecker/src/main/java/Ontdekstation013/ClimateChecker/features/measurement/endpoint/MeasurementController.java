package Ontdekstation013.ClimateChecker.features.measurement.endpoint;

import java.time.Instant;
import java.time.format.*;
import java.util.List;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.meetstation.Meetstation;
import Ontdekstation013.ClimateChecker.features.meetstation.MeetstationService;
import Ontdekstation013.ClimateChecker.features.meetstation.endpoint.MeetstationDto;
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
    private final MeetstationService meetstationService;

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
            List<MeasurementDTO> measurementList = measurementService.getMeasurementsAtTime(utcDateTime);
            for (MeasurementDTO measurementDTO : measurementList)
            {
                MeetstationDto meetstation = meetstationService.ReadById((long)measurementDTO.getId());
                if (meetstation != null) {
                    measurementDTO.setIs_public(meetstation.is_public);
                    measurementDTO.setUserId(meetstation.userid);
                } else {
                    measurementDTO.setIs_public(true); // or any default value you prefer
                }
            }
            return measurementList;
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

    try{
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime localDateTimeStart = LocalDateTime.parse(startDate, formatter);
        Instant startInstant = localDateTimeStart.atZone(ZoneId.systemDefault()).toInstant();

        LocalDateTime localDateTimeEnd = LocalDateTime.parse(endDate, formatter);
        Instant endInstant = localDateTimeEnd.atZone(ZoneId.systemDefault()).toInstant();

        if (startInstant.isAfter(endInstant)){
            throw new InvalidArgumentException("Start date is after end date");
        }
        return measurementService.getHistoricalMeasurements(id, startInstant, endInstant);
        }
        catch (Exception ex){
            throw ex;
        }
    }
}
