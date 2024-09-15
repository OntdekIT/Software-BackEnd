package Ontdekstation013.ClimateChecker.features.authentication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Mail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long MailId;

    private String header;
    private String body;
    private String footer;

    public Mail(String header, String body, String footer){
        this.header = header;
        this.body = body;
        this.footer = footer;
    }

}
