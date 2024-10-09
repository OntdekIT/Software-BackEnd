package Ontdekstation013.ClimateChecker.features.workshopcode;

import java.time.LocalDateTime;

public record WorkshopCodeRequest(Long code, LocalDateTime expirationDate) {
}
