package Ontdekstation013.ClimateChecker.user.authentication;

import Ontdekstation013.ClimateChecker.features.measurement.Measurement;
import Ontdekstation013.ClimateChecker.features.neighbourhood.Neighbourhood;
import Ontdekstation013.ClimateChecker.features.neighbourhood.NeighbourhoodCoords;
import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.station.StationMonitorService;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class EmailSenderServiceUnitTests {
    @Mock
    JavaMailSender mailSender;
    @InjectMocks
    private EmailSenderService emailSenderService;
    private Station station;
    private User user;
    private static String stationName = "StationName";
    private static Long stationId = 123L;
    private static String emailAdress = "email@email.com";
    private static String firstName = "FistName";
    private static String lastName = "LastName";
    @BeforeEach
    public void init() {
        station = new Station("Name", "MJS", true, stationId, 1L, 1L, true, true, true, true, true);
        station.setStationid(stationId);
        user = new User(1L, firstName, lastName, emailAdress, UserRole.USER, "Password123");
    }

    @Test
    public void testSendEmailStationMeasurements_withAllErrors_shouldSendEmail() throws MessagingException {
        //arrange
        MimeMessage expectedMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(expectedMessage, true);

        String body = "Het gedrag van een van je meetstations is veranderd. Het gaat om het volgende meetstation:"
                + "<br>"
                + "Nummer: " + stationId
                + "<br>"
                + "Naam: " + stationName
                + "<br>"
                + "<br>"
                + "De Temperatuur measurement van uw meetstation werkt weer. <br>"
                + "De Luchtvochtigheid measurement van uw meetstation werkt weer. <br>"
                + "De Fijnstof measurement van uw meetstation werkt weer. <br>"
                + "De Locatie measurement van uw meetstation werkt weer. <br>"
                + "<br>"
                + "<br>"
                + "Met vriendelijke groet,"
                + "<br>"
                + " Ontdekstation 013"
                + "<br>"
                + "<img src=\"cid:logo.png\"></img><br/>";

        helper.setTo(emailAdress);
        helper.setSubject(String.format("Hallo %s", firstName + " " + lastName));
        helper.setText(body, true);

        Boolean hasTemp = false;
        Boolean hasHum = false;
        Boolean hasStof = false;
        Boolean hasLoc = false;

        //act
        emailSenderService.sendEmailStationMeasurements(user, station, hasTemp, hasHum, hasStof, hasLoc);

        //assert
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage actualMessage = messageCaptor.getValue();
        assertSame(expectedMessage, actualMessage);
    }
}
