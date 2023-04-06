package Ontdekstation013.ClimateChecker.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class TranslationPage {

    public TranslationPage(){
        Bloks = new ArrayList<>();
    }
    @Id
    private int ID;
    private String LanguageID;
    @OneToMany
    public List<Translation> Bloks;
}
