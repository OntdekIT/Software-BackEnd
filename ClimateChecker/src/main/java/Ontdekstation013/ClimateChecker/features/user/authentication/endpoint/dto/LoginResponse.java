package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint.dto;

/**
 * Antwoord op een login-poging.
 * <p>
 * Bij een onbekend (niet-vertrouwd) IP is {@code verificationRequired} true en
 * is {@code token} null: er is een verificatiecode gemaild en de gebruiker moet
 * de verify-stap doorlopen. Bij een vertrouwd IP is {@code verificationRequired}
 * false en bevat {@code token} direct het JWT.
 */
public record LoginResponse(boolean verificationRequired, String token) {

    public static LoginResponse requiresVerification() {
        return new LoginResponse(true, null);
    }

    public static LoginResponse authenticated(String token) {
        return new LoginResponse(false, token);
    }
}
