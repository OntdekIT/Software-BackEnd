package Ontdekstation013.ClimateChecker.features.station.endpoint.dto;


/**
 * Filters for {@code GET /api/Meetstation/stations}. Without {@code page} and
 * {@code pageSize} all matching stations are returned as before; as soon as one of
 * them is given the result is paged (0-based page, default size 20) and the total
 * is returned in the {@code X-Total-Count} header.
 */
public record GetAllStationsRequest(
        Integer page,
        Integer pageSize,
        String name,
        String database_tag,
        Boolean isPublic,
        Long registrationCode,
        Long userid,
        String username,
        Boolean isActive
) {
    public boolean isPaged() {
        return page != null || pageSize != null;
    }
}
