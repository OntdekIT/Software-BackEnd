package Ontdekstation013.ClimateChecker.exception;

public class ExistingUniqueIdentifierException extends RuntimeException{
    public ExistingUniqueIdentifierException (String message) {
        super(message);
    }
}
