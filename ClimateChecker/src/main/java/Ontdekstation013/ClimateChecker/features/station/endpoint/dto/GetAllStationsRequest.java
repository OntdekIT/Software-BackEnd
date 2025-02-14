package Ontdekstation013.ClimateChecker.features.station.endpoint.dto;


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
    public GetAllStationsRequest {
        if (page == null) {
            page = 0;
        }

        if (pageSize == null) {
            pageSize = 20;
        }
    }
}
