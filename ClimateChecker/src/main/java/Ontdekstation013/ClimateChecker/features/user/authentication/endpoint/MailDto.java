package Ontdekstation013.ClimateChecker.features.user.authentication.endpoint;

public record MailDto(
        Long id,
        String header,
        String body,
        String footer
) {}
