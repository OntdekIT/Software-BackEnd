package Ontdekstation013.ClimateChecker.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Translation {

    @Id
//    private int ID;
    private String BoksID;
    private String Text;

    public Translation() {

    }
}
