package Ontdekstation013.ClimateChecker.services;

import Ontdekstation013.ClimateChecker.models.Location;
import Ontdekstation013.ClimateChecker.models.dto.createLocationDto;
import Ontdekstation013.ClimateChecker.models.dto.editLocationDto;
import Ontdekstation013.ClimateChecker.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public boolean editLocation(editLocationDto editLocationDto){
        boolean succes = false;
        Location currentLocation = locationRepository.findById(editLocationDto.getId()).get();
        if (currentLocation != null){
            currentLocation.setDirection(editLocationDto.getDirection());
            currentLocation.setHeight(editLocationDto.getHeight());
            currentLocation.setOutside(editLocationDto.isOutside());
            locationRepository.save(currentLocation);
            succes = true;
        }
        return succes;
    }

    public long createLocation(createLocationDto createLocationDto){
        boolean succes = false;

        Location location = new Location();
        location.setLatitude(createLocationDto.getLatitude());
        location.setLongitude(createLocationDto.getLongitude());
        Location locationResult = locationRepository.save(location);
        succes = true;

        return locationResult.getLocationID();
    }
}
