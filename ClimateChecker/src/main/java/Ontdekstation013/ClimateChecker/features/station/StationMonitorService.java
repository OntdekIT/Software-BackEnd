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
        scheduler.scheduleAtFixedRate(this::checkMeetstations, 0, 24, TimeUnit.HOURS);
    }

    private void checkMeetstations() {
        List<Station> meetstations = stationService.findAll(); // Fetch all meetstations

        for (Station station : meetstations) {
            try{
                if (station.getIsActive()){
                    MeetJeStadParameters params = new MeetJeStadParameters();
                    params.StationIds.add(station.getStationid().intValue());
                    params.StartDate = Instant.now().minusSeconds(24 * 60 * 60);

                    List<Measurement> measurements = meetJeStadService.getMeasurements(params);
                    if (measurements.isEmpty()){
                        User user = station.getUser();

                        if (user != null) {
                            emailSenderService.sendEmailStationDown(user, station);
                        }
                        station.setIsActive(false);
                        stationService.UpdateMeetstation(station);
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
