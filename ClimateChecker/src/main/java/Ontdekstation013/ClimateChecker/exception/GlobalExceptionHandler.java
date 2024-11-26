package Ontdekstation013.ClimateChecker.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {
        log.warn("NoSuchElementException: {}", ex.getMessage());
        String message = ex.getMessage().equals("No value present") ? "Resource not found" : ex.getMessage();
        return generateErrorResponseEntity(HttpStatus.NOT_FOUND, message);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        log.warn("NotFoundException: {}", ex.getMessage());
        return generateErrorResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage().isEmpty() ? "Resource not found" : ex.getMessage());
    }

    @ExceptionHandler(ExistingUniqueIdentifierException.class)
    public ResponseEntity<ErrorResponse> handleExistingUniqueIdentifierException(ExistingUniqueIdentifierException ex) {
        log.warn("ExistingUniqueIdentifierException: {}", ex.getMessage());
        return generateErrorResponseEntityFromException(HttpStatus.CONFLICT, ex, "Resource already exists");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return generateErrorResponseEntityFromException(HttpStatus.BAD_REQUEST, ex, "Invalid argument");
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgumentException(InvalidArgumentException ex) {
        log.warn("InvalidArgumentException: {}", ex.getMessage());
        return generateErrorResponseEntityFromException(HttpStatus.BAD_REQUEST, ex, "Invalid argument");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("MethodArgumentNotValidException: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return generateErrorResponseEntity(HttpStatus.BAD_REQUEST, errors.entrySet().stream()
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining(", ")));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("ConstraintViolationException: {}", ex.getMessage());
        return generateErrorResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private String getMessage(Throwable ex, String fallbackMessage) {
        return ex.getMessage() != null ? ex.getMessage() : fallbackMessage;
    }

    private ResponseEntity<ErrorResponse> generateErrorResponseEntityFromException(HttpStatus status, Throwable ex, String fallbackMessage) {
        return this.generateErrorResponseEntity(status, getMessage(ex, fallbackMessage));
    }

    private ResponseEntity<ErrorResponse> generateErrorResponseEntity(HttpStatus status, String message) {
        String timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        return new ResponseEntity<>(new ErrorResponse(timestamp, status.name(), status.value(), message), status);
    }
}