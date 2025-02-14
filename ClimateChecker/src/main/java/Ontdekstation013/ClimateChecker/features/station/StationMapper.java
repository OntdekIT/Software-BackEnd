package Ontdekstation013.ClimateChecker.features.station;

import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.station.endpoint.dto.GetAllStationsRequest;
import Ontdekstation013.ClimateChecker.features.user.UserFilter;
import Ontdekstation013.ClimateChecker.features.user.endpoint.dto.GetAllUsersRequest;

public class StationMapper {
    public static StationDto toStationDTO(Station station) {
        return new StationDto(
                station.getStationid(),
                station.getName(),
                station.getDatabase_tag(),
                station.getIs_public(),
                station.getRegistrationCode(),
                station.getLocation_locationid(),
                station.getUserid(),
                station.getIsActive()
        );
    }

    public static StationFilter toStationFilter(GetAllStationsRequest request) {
        return StationFilter.builder()
                .name(request.name())
                .databaseTag(request.database_tag())
                .isPublic(request.isPublic())
                .registrationCode(request.registrationCode())
                .username(request.username())
                .isActive(request.isActive())
                .build();
    }
}
