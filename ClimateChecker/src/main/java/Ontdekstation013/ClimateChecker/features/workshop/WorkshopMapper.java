package Ontdekstation013.ClimateChecker.features.workshop;

import Ontdekstation013.ClimateChecker.features.workshop.endpoint.dto.WorkshopResponse;

import java.time.ZoneOffset;

public class WorkshopMapper {
    private static final ZoneOffset utc = ZoneOffset.UTC;

    public static WorkshopResponse toResponse(Workshop workshop) {
        String expirationDate = workshop.getExpirationDate().atOffset(utc).toString();
        String creationDate = workshop.getCreationTime().atOffset(utc).toString();
        return new WorkshopResponse(
                workshop.getCode(),
                expirationDate,
                creationDate
        );
    }
}
