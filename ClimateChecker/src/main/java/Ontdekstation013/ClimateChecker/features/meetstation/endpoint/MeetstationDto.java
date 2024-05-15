package Ontdekstation013.ClimateChecker.features.meetstation.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeetstationDto {
    @JsonProperty("stationid")
    private Long stationid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("database_tag")
    private String database_tag;
    @JsonProperty("is_public")
    private Boolean is_public;
    @JsonProperty("registrationCode")
    private Long registrationCode;
    @JsonProperty("location_locationid")
    private Long location_locationid;
    @JsonProperty("userid")
    private Long userid;


    public MeetstationDto(Long stationid, String name, String database_tag, Boolean is_public, Long registrationCode, Long location_locationid, Long userid) {
        this.stationid = stationid;
        this.name = name;
        this.database_tag = database_tag;
        this.is_public = is_public;
        this.registrationCode = registrationCode;
        this.location_locationid = location_locationid;
        this.userid = userid;
    }
}
