package Ontdekstation013.ClimateChecker.features.user;

import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.workshop.Workshop;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long userId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @Enumerated(EnumType.ORDINAL)
    private UserRole role = UserRole.USER;

    private String password;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private Set<Station> stations;

    @ManyToOne
    @JoinColumn(name = "workshop_code")
    private Workshop workshop;

    public User(String firstName, String lastName, String email, String password, Workshop workshop) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.workshop = workshop;
    }

    public User(Long userId, String firstName, String lastName, String email, UserRole role, String password) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.password = password;
    }
}
