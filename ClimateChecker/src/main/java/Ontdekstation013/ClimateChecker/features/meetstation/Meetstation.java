package Ontdekstation013.ClimateChecker.features.meetstation;

import Ontdekstation013.ClimateChecker.features.meetstation.endpoint.MeetstationDto;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.endpoint.userDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "station")
@Getter
@Setter
@NoArgsConstructor
public class Meetstation {
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
    @Column(name = "registration_code")
    private Long registrationCode;
    @Column(name = "location_locationid")
    private Long location_locationid;
    @Column(name = "userid")
    private Long userid;

    @ManyToOne
    @JoinColumn(name = "userid", nullable = false, insertable = false, updatable = false)
    private User user;


    public Meetstation(String name, String database_tag, Boolean is_public, Long registrationCode, Long location_locationid, Long userid) {
        this.name = name;
        this.database_tag = database_tag;
        this.is_public = is_public;
        this.registrationCode = registrationCode;
        this.location_locationid = location_locationid;
        this.userid = userid;
    }

    public Meetstation(Boolean is_public, Long registrationCode) {
        this.is_public = is_public;
        this.registrationCode = registrationCode;
    }

    public MeetstationDto toDto(){
        return new MeetstationDto(stationid, name, database_tag, is_public, registrationCode, location_locationid, userid);
    }
}
