package Ontdekstation013.ClimateChecker.features.user.endpoint.dto;

import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.user.UserRole;

import java.util.Set;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        Set<StationDto> stations
) {}
