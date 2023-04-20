package Ontdekstation013.ClimateChecker.Services;

import Ontdekstation013.ClimateChecker.models.Location;
import Ontdekstation013.ClimateChecker.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location findLocationById(long id) {
        Location location = locationRepository.findById(id).get();
        return location;
    }

    public List<Location> getAll() {
        List<Location> locations = locationRepository.findAll();
        return locations;
    }
}
