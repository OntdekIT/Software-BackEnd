package Ontdekstation013.ClimateChecker.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import lombok.Getter;
import org.springframework.http.HttpStatus;

// LARS
@Getter
public class OntdekstationException {
    private final HttpStatus status;
    private final String message;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private final Instant timestampUTC;

    public OntdekstationException(HttpStatus status, String message){
        this.status = status;
        this.message = message;
        timestampUTC = Instant.now();
    }
}
