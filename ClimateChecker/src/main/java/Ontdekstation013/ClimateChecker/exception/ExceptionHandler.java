package Ontdekstation013.ClimateChecker.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// LARS
@Slf4j
@RestControllerAdvice
public class ExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public OntdekstationException handleException(Exception e){
        log.error("An unhandled exception occurred", e);
        return new OntdekstationException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidArgumentException.class)
    public OntdekstationException handleApiInvalidArgumentException(InvalidArgumentException e){
        log.warn("An InvalidArgumentException occurred", e);
        return new OntdekstationException(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @org.springframework.web.bind.annotation.ExceptionHandler
    public OntdekstationException handleNotFoundException(NotFoundException e){
        log.warn("A NotFoundException occurred", e);
        return new OntdekstationException(HttpStatus.NO_CONTENT,e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @org.springframework.web.bind.annotation.ExceptionHandler
    public OntdekstationException handleExistingUniqueIdentifierException (ExistingUniqueIdentifierException e){
        log.warn("An ExistingUniqueIdentifierException occurred", e);
        return new OntdekstationException(HttpStatus.CONFLICT, e.getMessage());
    }
}
