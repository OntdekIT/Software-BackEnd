package Ontdekstation013.ClimateChecker.features.measurement.endpoint;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.measurement.DataCoverageService;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodService;
import Ontdekstation013.ClimateChecker.features.station.StationService;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.utility.DataCoverageResponse;
import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import Ontdekstation013.ClimateChecker.utility.HourMeasurementResponse;
import Ontdekstation013.ClimateChecker.utility.HalfHourMeasurementResponse;
import Ontdekstation013.ClimateChecker.utility.NetworkMeasurementResponse;
import Ontdekstation013.ClimateChecker.utility.RegionAverageBucketResponse;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * For getting measurements of stations, only includes those inside the municipality of Tilburg.
 */
@RestController
@RequestMapping("/api/measurement")
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;
    private final StationService stationService;
    private final NeighbourhoodService neighbourhoodService;
    private final DataCoverageService dataCoverageService;

    /**
     * Gets the closest measurement to a given timestamp for each station.
     * Only includes measurements within {@link MeetJeStadService#getMinuteLimit() n} minutes of given timestamps past
     * @param timestamp - ISO 8601 format
     */
    @GetMapping("/history")
    public List<MeasurementDto> getMeasurementsAtTime(@RequestParam(value = "timestamp") String timestamp) {
        try {
            Instant utcDateTime = Instant.parse(timestamp);
            List<MeasurementDto> measurementList = measurementService.getMeasurementsAtTime(utcDateTime);
            for (MeasurementDto measurementDTO : measurementList)
            {
                StationDto meetstation = stationService.ReadById((long)measurementDTO.getId());
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

    /**
     * Geeft per dag de netwerkbrede statistiek: minimum, maximum, mediaan en
     * gemiddelde temperatuur over alle meetstations samen.
     *
     * <p>Gebruikt bewust hetzelfde datumformaat en dezelfde dagbuckets als
     * {@link #getMeasurementsAverage}, zodat de frontend de netwerkpanelen en het
     * paneel van het geselecteerde station over dezelfde periode kan tonen.
     *
     * @param startDate - dd-MM-yyyy HH:mm format
     * @param endDate   - dd-MM-yyyy HH:mm format
     */
    @GetMapping("/history/network")
    public List<NetworkMeasurementResponse> getNetworkMeasurements(@RequestParam String startDate,
                                                                   @RequestParam String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        Instant startInstant;
        Instant endInstant;
        try {
            startInstant = LocalDateTime.parse(startDate, formatter).atZone(ZoneId.systemDefault()).toInstant();
            endInstant = LocalDateTime.parse(endDate, formatter).atZone(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            throw new InvalidArgumentException("Dates must be in dd-MM-yyyy HH:mm format");
        }

        if (startInstant.isAfter(endInstant)) {
            throw new InvalidArgumentException("Start date is after end date");
        }

        return measurementService.getNetworkMeasurements(startInstant, endInstant);
    }

    /**
     * Returns aggregated temperature buckets (min/max/avg) for all stations within a region.
     *
     * @param regionId    ID of the neighbourhood/region
     * @param from        ISO 8601 start of time window (default: 24 h ago)
     * @param to          ISO 8601 end of time window (default: now)
     * @param granularity "half-hour", "hour" or "day" (default: "hour")
     */
    @GetMapping("/history/average/region/{regionId}")
    public List<RegionAverageBucketResponse> getMeasurementsAverageForRegion(
            @PathVariable Long regionId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "hour") String granularity) {

        Instant toInstant = (to != null) ? parseIso8601(to) : Instant.now();
        Instant fromInstant = (from != null) ? parseIso8601(from) : toInstant.minus(Duration.ofHours(24));

        if (fromInstant.isAfter(toInstant)) {
            throw new InvalidArgumentException("'from' must not be after 'to'");
        }

        return neighbourhoodService.getRegionAverageHistory(regionId, fromInstant, toInstant, granularity);
    }

    /**
     * Shows per station and for the whole network how complete the particulate matter data is,
     * so missing or irregular data can be spotted. Admin only (see SecurityConfiguration).
     *
     * @param from        ISO 8601 start of time window (default: 7 days before 'to')
     * @param to          ISO 8601 end of time window, exclusive (default: start of the current hour)
     * @param granularity "hour" or "day" (default: "hour")
     */
    @GetMapping("/coverage")
    public DataCoverageResponse getDataCoverage(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "hour") String granularity) {

        // Default to the start of the current hour so the last bucket is complete
        // and does not show up as a gap just because the hour is not over yet.
        Instant toInstant = (to != null) ? parseIso8601(to) : Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant fromInstant = (from != null) ? parseIso8601(from) : toInstant.minus(Duration.ofDays(7));

        return dataCoverageService.getPmCoverage(fromInstant, toInstant, granularity);
    }

    private Instant parseIso8601(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new InvalidArgumentException("Date must be in ISO 8601 format (e.g. 2025-05-10T14:00:00Z)");
        }
    }

    /**
     * Haalt uurlijkse gemiddeldes op (temp en PM2.5) van een specifiek station voor de huidige dag (tot nu).
     * @param id - stationId
     */
    @GetMapping("/history/hourly/{id}")
    public List<HourMeasurementResponse> getTodayHourlyMeasurements(@PathVariable int id) {
        try {
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endOfCurrentHour = Instant.now();

            return measurementService.getHourlyMeasurements(id, startOfDay, endOfCurrentHour);
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Haalt halfuurlijkse gemiddeldes op (temp en PM2.5) van een specifiek station voor de huidige dag (tot nu).
     * @param id - stationId
     */
    @GetMapping("/history/half-hourly/{id}")
    public List<HalfHourMeasurementResponse> getTodayHalfHourlyMeasurements(@PathVariable int id) {
        try {
            LocalDate today = LocalDate.now(ZoneId.systemDefault());
            Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endOfCurrentHour = Instant.now();

            return measurementService.getHalfHourlyMeasurements(id, startOfDay, endOfCurrentHour);
        } catch (Exception ex) {
            throw ex;
        }
    }
}
