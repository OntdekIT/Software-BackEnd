package Ontdekstation013.ClimateChecker.exception;

/** Thrown when a client exceeds the attempt limit of an authentication endpoint (answered with 429). */
public class TooManyAttemptsException extends RuntimeException {
    public TooManyAttemptsException() {
        super("Too many attempts");
    }
}
