package Ontdekstation013.ClimateChecker.features.station;

import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;

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
}
