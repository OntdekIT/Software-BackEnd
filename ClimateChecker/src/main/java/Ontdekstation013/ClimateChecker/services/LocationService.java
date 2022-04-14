package Ontdekstation013.ClimateChecker.services;

import Ontdekstation013.ClimateChecker.models.Location;
import Ontdekstation013.ClimateChecker.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location findLocationById(Long id) {
        Location location = locationRepository.findById(id).get();
        return location;
    }
}
