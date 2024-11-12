package Ontdekstation013.ClimateChecker.features.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationResult {
    private boolean isValid;
    private String errorMessage;

    public ValidationResult(boolean isValid, String errorMessage) {
        this.isValid = isValid;
        this.errorMessage = errorMessage;
    }
}
