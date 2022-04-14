package Ontdekstation013.ClimateChecker.models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long LocationID;

    private String locationName;

    @NotBlank
    private float latitude;

    @NotBlank
    private float longitude;

    @OneToOne(mappedBy = "location")
    private Station station;
}
