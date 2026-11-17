package Ontdekstation013.ClimateChecker.user.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import Ontdekstation013.ClimateChecker.features.station.Station;
import Ontdekstation013.ClimateChecker.features.user.User;
import Ontdekstation013.ClimateChecker.features.user.UserRole;
import Ontdekstation013.ClimateChecker.features.user.authentication.EmailSenderService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

public class EmailSenderServiceTest {
    private JavaMailSender mailSender;
    private EmailSenderService enabledService;
    private User user;
    private Station station;

    @BeforeEach
    public void setUp() {
        mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenAnswer(i -> new MimeMessage(Session.getInstance(new Properties())));
        enabledService = new EmailSenderService(mailSender, true);
        ReflectionTestUtils.setField(enabledService, "frontendHost", "http://localhost:5173");

        user = new User(1L, "Jan", "Jansen", "jan@example.com", UserRole.USER, "Password");
        station = new Station();
        station.setStationid(7L);
        station.setName("Dak");
    }

    @Test
    public void sendEmail_sendsSimpleMessageToRecipient() {
        enabledService.sendEmail("jan@example.com", "Jan", "Jansen", "hallo");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertEquals("jan@example.com", captor.getValue().getTo()[0]);
    }

    @Test
    public void deleteUserMail_sendsSimpleMessage() {
        enabledService.deleteUserMail("jan@example.com", "Jan", "Jansen");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    public void mimeMails_areSentThroughTheMailSender() throws Exception {
        enabledService.sendSignupMail("jan@example.com", "Jan", "Jansen", "123456");
        enabledService.sendLoginMail("jan@example.com", "Jan", "Jansen", "123456");
        enabledService.sendForgotPasswordMail("jan@example.com", "Jan", "Jansen", "token");
        enabledService.sendEmailEditMail("jan@example.com", "Jan", "Jansen", "123456");
        enabledService.sendEmailStationDown(user, station);
        enabledService.sendEmailStationMeasurements(user, station, true, false, null, true);

        org.mockito.Mockito.verify(mailSender, org.mockito.Mockito.times(6)).send(any(MimeMessage.class));
    }

    @Test
    public void forgotPasswordMail_containsResetLinkWithToken() throws Exception {
        enabledService.sendForgotPasswordMail("jan@example.com", "Jan", "Jansen", "geheimetoken");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage sent = captor.getValue();
        sent.saveChanges();
        String content = new String(sent.getInputStream().readAllBytes());
        org.junit.jupiter.api.Assertions.assertTrue(content.contains("token=geheimetoken"));
    }

    @Test
    public void nothingIsSent_whenRealMailserverIsDisabled() throws Exception {
        EmailSenderService disabled = new EmailSenderService(mailSender, false);

        disabled.sendEmail("jan@example.com", "Jan", "Jansen", "hallo");
        disabled.deleteUserMail("jan@example.com", "Jan", "Jansen");
        disabled.sendSignupMail("jan@example.com", "Jan", "Jansen", "123456");
        disabled.sendEmailStationDown(user, station);

        verifyNoInteractions(mailSender);
    }

    @Test
    public void sendFailure_isPropagatedToTheCaller() {
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(MailSendException.class,
                () -> enabledService.sendEmail("jan@example.com", "Jan", "Jansen", "hallo"));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
