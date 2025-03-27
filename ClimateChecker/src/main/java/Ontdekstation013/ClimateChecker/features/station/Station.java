package Ontdekstation013.ClimateChecker.features.station;

import Ontdekstation013.ClimateChecker.features.station.endpoint.StationDto;
import Ontdekstation013.ClimateChecker.features.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "station")
@Getter
@Setter
@NoArgsConstructor
public class Station {
    @Id
    @Column(name = "stationid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stationid;

    @Column(name = "name")
    private String name;

    @Column(name = "database_tag")
    private String database_tag;

    @Column(name = "is_public")
    private Boolean is_public;

    @Column(name = "registration_code", unique = true, nullable = false)
    private Long registrationCode;

    @Column(name = "location_locationid")
    private Long location_locationid;
    @Column(name = "user_id")
    private Long userid;

    @Column(name = "is_active") // Nieuwe kolom voor de status
    private Boolean isActive;

    @Column(name = "temp_error")
    private Boolean tempError;

    @Column(name = "hum_error")
    private Boolean humError;

    @Column(name = "stof_error")
    private Boolean stofError;

    @Column(name = "loc_error")
    private Boolean locError;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", nullable = true, insertable = false, updatable = false)
    private User user;

    public Station(String name, String database_tag, Boolean is_public, Long registrationCode, Long location_locationid, Long userid, Boolean isActive, Boolean tempError, Boolean humError, Boolean stofError, Boolean locError) {
        this.name = name;
        this.database_tag = database_tag;
        this.is_public = is_public;
        this.registrationCode = registrationCode;
        this.location_locationid = location_locationid;
        this.userid = userid;
        this.isActive = isActive;
        this.tempError = tempError;
        this.humError = humError;
        this.stofError = stofError;
        this.locError = locError;
    }

    public StationDto toDto() {
        return new StationDto(stationid, name, database_tag, is_public, registrationCode, location_locationid, userid , isActive, tempError, humError, stofError, locError);
    }

    public Station(StationDto stationDto) {
        this.stationid = stationDto.stationid;
        this.name = stationDto.name;
        this.database_tag = stationDto.database_tag;
        this.is_public = stationDto.is_public;
        this.registrationCode = stationDto.registrationCode;
        this.location_locationid = stationDto.location_locationid;
        this.userid = stationDto.userid;
        this.isActive = stationDto.isActive;
        this.tempError = stationDto.tempError;
        this.humError = stationDto.humError;
        this.stofError = stationDto.stofError;
        this.locError = stationDto.locError;
    }
}

