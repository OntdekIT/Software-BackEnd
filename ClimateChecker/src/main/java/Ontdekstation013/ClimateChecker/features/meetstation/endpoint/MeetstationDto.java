package Ontdekstation013.ClimateChecker.features.meetstation.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeetstationDto {
    @JsonProperty("stationid")
    public Long stationid;
    @JsonProperty("name")
    public String name;
    @JsonProperty("database_tag")
    public String database_tag;
    @JsonProperty("is_public")
    public Boolean is_public;
    @JsonProperty("registrationCode")
    public Long registrationCode;
    @JsonProperty("location_locationid")
    public Long location_locationid;
    @JsonProperty("userid")
    public Long userid;


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
