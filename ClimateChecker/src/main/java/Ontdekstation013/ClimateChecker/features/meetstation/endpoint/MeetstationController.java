package Ontdekstation013.ClimateChecker.features.meetstation.endpoint;

import Ontdekstation013.ClimateChecker.exception.InvalidArgumentException;
import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.measurement.MeasurementService;
import Ontdekstation013.ClimateChecker.features.meetstation.Meetstation;
import Ontdekstation013.ClimateChecker.features.meetstation.MeetstationService;
import Ontdekstation013.ClimateChecker.features.user.endpoint.userDto;
import Ontdekstation013.ClimateChecker.utility.DayMeasurementResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/Meetstation")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class MeetstationController {
    private final MeetstationService meetstationService;
    private final MeasurementService measurementService;

    public MeetstationController (MeetstationService meetstationService, MeasurementService measurementService){
        this.meetstationService = meetstationService;
        this.measurementService = measurementService;
    }

    @GetMapping("{id}")
    public ResponseEntity<MeetstationDto> getMeetstation(@PathVariable Long id) throws Exception {
        try{
            if (id != 0){
                MeetstationDto meetstationDto = meetstationService.ReadById(id);
                return ResponseEntity.status(HttpStatus.OK).body(meetstationDto);
            }
        }
        catch (Exception error){

        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PutMapping("")
    public ResponseEntity<String> updateMeetstation(@RequestBody MeetstationDto meetstationDto) throws Exception {
        try{
            meetstationService.UpdateMeetstation(new Meetstation((meetstationDto)));
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
        catch (Exception ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/measurements/{id}")
    public ResponseEntity<byte[]> getMeetstationMeasurements(@PathVariable int id, @RequestParam String startDate, @RequestParam String endDate){
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            LocalDateTime localDateTimeStart = LocalDateTime.parse(startDate, formatter);
            Instant startInstant = localDateTimeStart.atZone(ZoneId.systemDefault()).toInstant();

            LocalDateTime localDateTimeEnd = LocalDateTime.parse(endDate, formatter);
            Instant endInstant = localDateTimeEnd.atZone(ZoneId.systemDefault()).toInstant();

            if (startInstant.isAfter(endInstant)){
                throw new InvalidArgumentException("Start date is after end date");
            }
            byte[] pdfBytes = measurementService.getMeasurementsAsPDF(id, startInstant, endInstant);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "measurements.pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        }
        catch (Exception ex){
            throw ex;
        }
    }
}
