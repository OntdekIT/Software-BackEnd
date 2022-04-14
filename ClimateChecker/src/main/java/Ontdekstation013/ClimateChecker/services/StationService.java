package Ontdekstation013.ClimateChecker.services;

import Ontdekstation013.ClimateChecker.models.Station;
import Ontdekstation013.ClimateChecker.repositories.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StationService {

    private final StationRepository stationRepository;

    @Autowired
    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public Station findStationById(Long id) {
        Station station = stationRepository.findById(id).get();
        return station;
    }
}
