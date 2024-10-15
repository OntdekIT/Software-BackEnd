package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.UserDto;

import java.util.HashSet;
import java.util.Set;

public class UserMapper {
    public static UserDto toDto(User user) {
        Set<StationDto> stationDtos = new HashSet<StationDto>();
        for (Station station : user.getStations()) {
            stationDtos.add(station.toDto());
        }
        return new UserDto(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isAdmin(),
                stationDtos);
    }
}
