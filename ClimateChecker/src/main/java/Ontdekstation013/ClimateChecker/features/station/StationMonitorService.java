package Ontdekstation013.ClimateChecker.features.station;

import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadParameters;
import Ontdekstation013.ClimateChecker.features.meetjestad.MeetJeStadService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
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
        scheduler.scheduleAtFixedRate(this::checkMeetstations, 0, 15, TimeUnit.SECONDS);
    }

    private void checkMeetstations() {
        List<Station> meetstations = stationService.findAll(); // Fetch all meetstations

        for (Station station : meetstations) {
            try{
                MeetJeStadParameters params = new MeetJeStadParameters();
                params.StationIds.add(station.getStationid().intValue());
                params.StartDate = Instant.now().minusSeconds(24 * 60 * 60);
                params.includeFaultyMeasurements = true;

                List<Measurement> measurements = meetJeStadService.getMeasurements(params);
                if (measurements.isEmpty()){

                    if (station.getIsActive()){
                        station.setIsActive(false);
                        stationService.UpdateMeetstation(station);

                        User user = station.getUser();

                        if (user != null) {
                            emailSenderService.sendEmailStationDown(user, station);
                        }
                    }
                }
                else{
                    if (!station.getIsActive()){
                        station.setIsActive(true);
                        stationService.UpdateMeetstation(station);
                    }
                }
                if (station.getIsActive()){

                    Boolean hasTemp = false;
                    Boolean hasHum = false;
                    Boolean hasStof = false;
                    Boolean hasLoc = false;
                    for (Measurement measurement : measurements) {
                        if (measurement.getTemperature() != null){
                            hasTemp = true;
                        }
                        if (measurement.getHumidity() != null){
                            hasHum = true;
                        }
                        if (measurement.getParticulate() != null){
                            hasStof = true;
                        }
                        if (!Float.isNaN(measurement.getLatitude()) && !Float.isNaN(measurement.getLongitude())) {
                            hasLoc = true;
                        }
                    }

                    Boolean sendEmailTemp = null;
                    Boolean sendEmailHum = null;
                    Boolean sendEmailStof = null;
                    Boolean sendEmailLoc = null;

                    if (!(station.getTempError().equals(!hasTemp))) {
                        station.setTempError(!hasTemp); // Update state
                        sendEmailTemp = !hasTemp; // Return the new state if changed
                    }
                    if (!(station.getHumError().equals(!hasHum))) {
                        station.setHumError(!hasHum); // Update state
                        sendEmailHum = !hasHum; // Return the new state if changed
                    }
                    if (!(station.getStofError().equals(!hasStof))) {
                        station.setStofError(!hasStof); // Update state
                        sendEmailStof = !hasStof; // Return the new state if changed
                    }
                    if (!(station.getLocError().equals(!hasLoc))) {
                        station.setLocError(!hasLoc); // Update state
                        sendEmailLoc = !hasLoc; // Return the new state if changed
                    }

                    stationService.UpdateMeetstation(station);
                    User user = station.getUser();
                    if (sendEmailTemp != null || sendEmailHum != null || sendEmailStof != null || sendEmailLoc != null){ //Check if status of meetstation has been changed
                        emailSenderService.sendEmailStationMeasurements(user, station, sendEmailTemp, sendEmailHum, sendEmailStof, sendEmailLoc);
                    }
                }
            }
            catch (Exception ex){
                //handle exception
            }
        }
    }




    public void shutdown() {
        scheduler.shutdown();
    }
}
