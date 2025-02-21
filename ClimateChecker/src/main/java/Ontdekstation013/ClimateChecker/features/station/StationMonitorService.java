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
                if (station.getIsActive()){
                    MeetJeStadParameters params = new MeetJeStadParameters();
                    params.StationIds.add(station.getStationid().intValue());
                    params.StartDate = Instant.now().minusSeconds(24 * 60 * 60);
                    params.includeFaultyMeasurements = true;

                    List<Measurement> measurements = meetJeStadService.getMeasurements(params);
                    if (measurements.isEmpty()){
                        User user = station.getUser();

                        if (user != null) {
                            emailSenderService.sendEmailStationDown(user, station);
                        }
                        station.setIsActive(false);
                        stationService.UpdateMeetstation(station);
                    }
                    else{
                        Boolean hasTemp = false;
                        Boolean hasHum = false;
                        Boolean hasPart = false;
                        for (Measurement measurement : measurements) {
                            if (measurement.getTemperature() != null){
                                hasTemp = true;
                            }
                            if (measurement.getHumidity() != null){
                                hasHum = true;
                            }
                            if (measurement.getParticulate() != null){
                                hasPart = true;
                            }
                        }

                        Boolean sendEmailTemp = null;
                        Boolean sendEmailHum = null;
                        Boolean sendEmailPart = null;

                        if (!station.getTempError() != hasTemp){
                            sendEmailTemp = hasTemp; //True = Fixed , False = Broken
                            station.setTempError(!hasTemp);
                        }
                        if (!station.getHumError() != hasHum){
                            sendEmailHum = hasHum; //True = Fixed , False = Broken
                            station.setHumError(!hasHum);
                        }
                        if (!station.getPartError() != hasPart){
                            sendEmailPart = hasPart; //True = Fixed , False = Broken
                            station.setPartError(!hasPart);
                        }
                        stationService.UpdateMeetstation(station);
                        User user = station.getUser();
                        if (sendEmailTemp != null || sendEmailHum != null || sendEmailPart != null){
                            emailSenderService.sendEmailStationMeasurements(user, station, sendEmailTemp, sendEmailHum, sendEmailPart);
                        }
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
