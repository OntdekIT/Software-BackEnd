package Ontdekstation013.ClimateChecker.features.station.endpoint;

import Ontdekstation013.ClimateChecker.features.measurement.endpoint.MeasurementDto;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class StationDto {
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
    @JsonProperty("latitude")
    public float latitude;
    @JsonProperty("longitude")
    public float longitude;
    @JsonProperty("userid")
    public Long userid;
    @JsonProperty("isActive")
    public Boolean isActive;
    @JsonProperty("measurements")
    public List<MeasurementDto> measurementDtoList;

    @JsonProperty("tempError")
    public Boolean tempError;
    @JsonProperty("humError")
    public Boolean humError;
    @JsonProperty("stofError")
    public Boolean stofError;
    @JsonProperty("locError")
    public Boolean locError;

    public StationDto(Long stationid, String name, String database_tag, Boolean is_public, Long registrationCode, Long location_locationid, Long userid, Boolean isActive, Boolean tempError, Boolean humError, Boolean stofError, Boolean locError) {
        this.stationid = stationid;
        this.name = name;
        this.database_tag = database_tag;
        this.is_public = is_public;
        this.registrationCode = registrationCode;
        this.location_locationid = location_locationid;
        this.userid = userid;
        this.isActive = isActive;
        this.measurementDtoList = null;
        this.tempError = tempError;
        this.humError = humError;
        this.stofError = stofError;
        this.locError = locError;
    }

    public StationDto(Long stationid, String name, String database_tag, Boolean is_public, Long registrationCode, Long location_locationid, Long userid, Boolean isActive, List<MeasurementDto> measurementDtoList) {
        this.stationid = stationid;
        this.name = name;
        this.database_tag = database_tag;
        this.is_public = is_public;
        this.registrationCode = registrationCode;
        this.location_locationid = location_locationid;
        this.userid = userid;
        this.isActive = isActive;
        this.measurementDtoList = measurementDtoList;
    }

    public StationDto(Long stationid, String name, String database_tag, Boolean is_public, Long registrationCode, Long location_locationid, float latitude, float longitude, Long userid, Boolean isActive, List<MeasurementDto> measurementDtoList, Boolean tempError, Boolean humError, Boolean stofError, Boolean locError) {
        this.stationid = stationid;
        this.name = name;
        this.database_tag = database_tag;
        this.is_public = is_public;
        this.registrationCode = registrationCode;
        this.location_locationid = location_locationid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userid = userid;
        this.isActive = isActive;
        this.measurementDtoList = measurementDtoList;
        this.tempError = tempError;
        this.humError = humError;
        this.stofError = stofError;
        this.locError = locError;
    }

    public StationDto() {

    }

    public StationDto(Long stationid, String name, String databaseTag, Boolean isPublic, Long registrationCode, Long locationLocationid, Long userid, Object o) {
    }
}
