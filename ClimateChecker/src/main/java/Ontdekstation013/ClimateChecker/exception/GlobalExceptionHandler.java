package Ontdekstation013.ClimateChecker.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
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

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("MethodArgumentNotValidException: {}", ex.getMessage());
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            errors.put(fieldName, error.getDefaultMessage());
        });

        return new ResponseEntity<>(ErrorResponse.of(HttpStatus.BAD_REQUEST, errors.entrySet().stream()
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining(", "))), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("HttpMessageNotReadableException: {}", ex.getMessage());
        return new ResponseEntity<>(ErrorResponse.of(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body"), HttpStatus.BAD_REQUEST);
    }

    /** Gives every exception handled by Spring MVC itself (405, 415, missing parameter, ...) the standard error body. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = body instanceof ProblemDetail problem && problem.getDetail() != null
                ? problem.getDetail()
                : getMessage(ex, status.getReasonPhrase());
        return new ResponseEntity<>(ErrorResponse.of(status, message), headers, status);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("ConstraintViolationException: {}", ex.getMessage());
        return generateErrorResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("AccessDeniedException: {}", ex.getMessage());
        return generateErrorResponseEntity(HttpStatus.FORBIDDEN, getMessage(ex, "Access denied"));
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParseException(DateTimeParseException ex) {
        log.warn("DateTimeParseException: {}", ex.getMessage());
        return generateErrorResponseEntity(HttpStatus.BAD_REQUEST, "Invalid date or time: '" + ex.getParsedString() + "'");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("DataIntegrityViolationException: {}", ex.getMessage());
        return generateErrorResponseEntity(HttpStatus.CONFLICT, "Resource already exists or conflicts with existing data");
    }

    /** Safety net: anything unhandled becomes a 500 with the standard body and never leaks internals. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error", ex);
        return generateErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Er is een onverwachte fout opgetreden.");
    }

    private String getMessage(Throwable ex, String fallbackMessage) {
        return ex.getMessage() != null ? ex.getMessage() : fallbackMessage;
    }

    private ResponseEntity<ErrorResponse> generateErrorResponseEntityFromException(HttpStatus status, Throwable ex, String fallbackMessage) {
        return this.generateErrorResponseEntity(status, getMessage(ex, fallbackMessage));
    }

    private ResponseEntity<ErrorResponse> generateErrorResponseEntity(HttpStatus status, String message) {
        return new ResponseEntity<>(ErrorResponse.of(status, message), status);
    }
}