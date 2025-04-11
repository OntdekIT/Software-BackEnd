package Ontdekstation013.ClimateChecker.features.station;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class StationMonitorService {
    private final StationService stationService;
    private final MeetJeStadService meetJeStadService;
    private final EmailSenderService emailSenderService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    @Autowired
    public StationMonitorService(StationService stationService, MeetJeStadService meetJeStadService, EmailSenderService emailSenderService) {
        this.stationService = stationService;
        this.meetJeStadService = meetJeStadService;
        this.emailSenderService = emailSenderService;
    }

    @PostConstruct
    public void init() {
        scheduleCheck();
    }

    private void scheduleCheck() {
        scheduler.scheduleAtFixedRate(this::checkMeetstations, 0, 15, TimeUnit.SECONDS); //The actual check interval should be longer, To be discussed with stakeholders.
    }

    public void checkMeetstations() {
        List<Station> meetstations = stationService.findAll();

        for (Station station : meetstations) {
            try {
                List<Measurement> measurements = fetchMeasurementsForStation(station);

                if (measurements.isEmpty()) {
                    handleInactiveStation(station);
                } else {
                    handleActiveStation(station, measurements);
                }

            } catch (Exception ex) {
                System.out.print("Er is iets fout gegaan bij het checken van de meetstations. Exception: " + ex);
            }
        }
    }

    private List<Measurement> fetchMeasurementsForStation(Station station) {
        try {
            MeetJeStadParameters params = new MeetJeStadParameters();
            params.StationIds.add(station.getStationid().intValue());
            params.StartDate = Instant.now().minusSeconds(24 * 60 * 60);
            params.includeFaultyMeasurements = true;
            return meetJeStadService.getMeasurements(params);
        }
        catch (Exception ex){
            throw ex;
        }
    }

    private void handleInactiveStation(Station station) throws MessagingException {
        try{
            if (station.getIsActive()) {
                station.setIsActive(false);
                stationService.UpdateMeetstation(station);
                User user = station.getUser();
                if (user != null) {
                    emailSenderService.sendEmailStationDown(user, station);
                }
            }
        }
        catch (Exception ex){
            throw ex;
        }
    }

    private void handleActiveStation(Station station, List<Measurement> measurements) throws MessagingException {
        try {
            if (!station.getIsActive()) {
                station.setIsActive(true);
                stationService.UpdateMeetstation(station);
            }

            Map<String, Boolean> measurementPresence = analyzeMeasurements(measurements);

            Boolean sendEmailTemp = updateErrorStatus(station, "Temp", measurementPresence.get("temp"));
            Boolean sendEmailHum = updateErrorStatus(station, "Hum", measurementPresence.get("hum"));
            Boolean sendEmailStof = updateErrorStatus(station, "Stof", measurementPresence.get("stof"));
            Boolean sendEmailLoc = updateErrorStatus(station, "Loc", measurementPresence.get("loc"));

            stationService.UpdateMeetstation(station);

            if (sendEmailTemp != null || sendEmailHum != null || sendEmailStof != null || sendEmailLoc != null) {
                User user = station.getUser();
                emailSenderService.sendEmailStationMeasurements(user, station, sendEmailTemp, sendEmailHum, sendEmailStof, sendEmailLoc);
            }
        }
        catch (Exception ex){
            throw ex;
        }
    }

    private Map<String, Boolean> analyzeMeasurements(List<Measurement> measurements) {
        boolean hasTemp = false;
        boolean hasHum = false;
        boolean hasStof = false;
        boolean hasLoc = false;

        for (Measurement m : measurements) {
            if (m.getTemperature() != null) hasTemp = true;
            if (m.getHumidity() != null) hasHum = true;
            if (m.getParticulate() != null) hasStof = true;
            if (!Float.isNaN(m.getLatitude()) && !Float.isNaN(m.getLongitude())) hasLoc = true;
        }

        Map<String, Boolean> result = new HashMap<>();
        result.put("temp", hasTemp);
        result.put("hum", hasHum);
        result.put("stof", hasStof);
        result.put("loc", hasLoc);
        return result;
    }

    private Boolean updateErrorStatus(Station station, String type, boolean hasMeasurement) {
        switch (type.toLowerCase()) {
            case "temp":
                if (station.getTempError().equals(hasMeasurement)) {
                    station.setTempError(!station.getTempError());
                    return hasMeasurement;
                }
                return null;
            case "hum":
                if (station.getHumError().equals(hasMeasurement)) {
                    station.setHumError(!station.getHumError());
                    return hasMeasurement;
                }
                return null;
            case "stof":
                if (station.getStofError().equals(hasMeasurement)) {
                    station.setStofError(!station.getStofError());
                    return hasMeasurement;
                }
                return null;
            case "loc":
                if (station.getLocError().equals(hasMeasurement)) {
                    station.setLocError(!station.getLocError());
                    return hasMeasurement;
                }
                return null;
        }
        return null;
    }




    public void shutdown() {
        scheduler.shutdown();
    }
}
