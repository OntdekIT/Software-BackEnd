package Ontdekstation013.ClimateChecker.features.workshop;

import Ontdekstation013.ClimateChecker.features.workshop.endpoint.dto.WorkshopResponse;

public class WorkshopMapper {
    public static WorkshopResponse toResponse(Workshop workshop) {
        return new WorkshopResponse(
                workshop.getCode(),
                String.valueOf(workshop.getExpirationDate()),
                String.valueOf(workshop.getCreationTime())
        );
    }
}
