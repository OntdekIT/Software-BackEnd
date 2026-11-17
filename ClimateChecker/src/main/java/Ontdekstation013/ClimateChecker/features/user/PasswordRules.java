package Ontdekstation013.ClimateChecker.features.user;

/**
 * Wachtwoordeisen die voor registreren en resetten gelden: 8 tot 128 tekens,
 * minstens een hoofdletter en een cijfer.
 */
public final class PasswordRules {

    public static final String PATTERN = "^(?=.*[A-Z])(?=.*\\d).{8,128}$";
    public static final String MESSAGE =
            "must be 8 to 128 characters long, contain at least one capital letter and one number";

    private PasswordRules() {
    }
}
