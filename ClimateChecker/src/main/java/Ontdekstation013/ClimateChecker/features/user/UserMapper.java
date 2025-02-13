package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationMapper;
import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto.RegisterUserRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.GetAllUsersRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UpdateMyAccountRequest;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.UserResponse;
import Ontdekstation013.ClimateChecker.features.workshop.Workshop;

import java.util.HashSet;
import java.util.Set;

public class UserMapper {
    public static UserResponse toUserResponse(User user, boolean includeStations) {
        Set<StationDto> stationDTOs = new HashSet<>();

        if (includeStations) {
            stationDTOs = toStationDTOs(user.getStations());
        }

        return new UserResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                stationDTOs
        );
    }

    public static User toUser(RegisterUserRequest request, String hashedPassword, Workshop workshop) {
        return new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                hashedPassword,
                workshop
        );
    }

    public static User toUpdatedUser(User user, UpdateMyAccountRequest request) {
        return new User(
                user.getUserId(),
                request.firstName(),
                request.lastName(),
                request.email(),
                user.getRole(),
                request.password()
        );
    }

    public static UserFilter toUserFilter(GetAllUsersRequest request) {
        return UserFilter.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .role(request.role())
                .build();
    }

    private static Set<StationDto> toStationDTOs(Set<Station> stations) {
        Set<StationDto> stationDTOs = new HashSet<>();

        for (Station station : stations) {
            stationDTOs.add(StationMapper.toStationDTO(station));
        }

        return stationDTOs;
    }
}
