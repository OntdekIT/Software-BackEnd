package Ontdekstation013.ClimateChecker.exception;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;

public record ErrorResponse(String timestamp, String error, int statusCode, String message) {

    /** Builds the standard error body used by every error response of the API. */
    public static ErrorResponse of(HttpStatus status, String message) {
        String timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        return new ErrorResponse(timestamp, status.name(), status.value(), message);
    }
}
