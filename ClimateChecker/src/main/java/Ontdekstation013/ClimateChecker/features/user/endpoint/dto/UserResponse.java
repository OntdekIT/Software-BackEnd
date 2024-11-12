package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;

import java.util.Set;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean isAdmin,
        Set<StationDto> stations
) {}
