package Ontdekstation013.ClimateChecker.features.workshopCode;

import java.time.LocalDateTime;

public record WorkshopCodeRequest(Long code, LocalDateTime expirationDate) {
}
