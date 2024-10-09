package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserDataDto;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserDto;

import java.util.HashSet;
import java.util.Set;

public class UserMapper {
    public static UserDataDto userToUserDataDto(User user) {
        UserDataDto dto = new UserDataDto();
        dto.setId(user.getUserId());
        dto.setMailAddress(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPassword(user.getPassword());
        return dto;
    }

    public static UserDto userToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getUserId());
        dto.setMailAddress(user.getEmail());
        dto.setLastName(user.getLastName());
        dto.setFirstName(user.getFirstName());
        dto.setPassword(user.getPassword());
        dto.setAdmin(user.getAdmin());
        return dto;
    }

    public static UserDto toDto() {
        Set<StationDto> stationDtos = new HashSet<StationDto>();
        for (Station station : stations) {
            stationDtos.add(station.toDto());
        }
        return new UserDto(userId, firstName, lastName, mailAddress, isAdmin, stationDtos);
    }
}
