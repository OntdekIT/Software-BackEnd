package Ontdekstation013.ClimateChecker.exception;

public record ErrorResponse(String timestamp, String error, int statusCode, String message) {}