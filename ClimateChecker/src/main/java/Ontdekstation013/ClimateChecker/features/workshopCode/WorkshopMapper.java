package Ontdekstation013.ClimateChecker.features.workshopCode;

import Ontdekstation013.ClimateChecker.features.workshopCode.endpoint.dto.WorkshopResponse;

public class WorkshopMapper {
    public static WorkshopResponse toResponse(Workshop workshop) {
        return new WorkshopResponse(
                workshop.getCode(),
                String.valueOf(workshop.getExpirationDate()),
                String.valueOf(workshop.getCreationTime())
        );
    }
}
