package Ontdekstation013.ClimateChecker.features.workshop.endpoint.dto;

public record WorkshopResponse(
    Long code,
    String expirationDate,
    String creationDate
) {}
