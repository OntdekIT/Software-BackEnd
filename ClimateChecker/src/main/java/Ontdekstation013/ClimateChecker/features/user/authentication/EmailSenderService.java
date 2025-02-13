package Ontdekstation013.ClimateChecker.features.user.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailSenderService {
    private final JavaMailSender mailSender;

    @Value("${frontend.host}")
    private String frontendHost;

    public EmailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String toEmail, String firstName, String lastName, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("kayletmail@gmail.com");
        message.setTo(toEmail);
        message.setSubject(String.format("Welcome %s", firstName + " " + lastName));
        message.setText(String.format("Welcome %s", body));


        mailSender.send(message);

        System.out.print("Mail Send");
    }

    public void sendSignupMail(String toEmail, String firstName, String lastName, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String body = "Je hebt net je account aangemaakt bij Ontdekstation 013,"
                + "<br>"
                + "<br>"
                + "<br>"
                + code
                + "<br>"
                + "<br>"
                + "Met vriendelijke groet,"
                + "<br>"
                + " Ontdekstation 013"
                + "<br>"
                + "<img src=\"cid:logo.png\"></img><br/>";

        helper.setTo("kayletmail@host.com");
        helper.setTo(toEmail);
        helper.setSubject(String.format("Welkom %s", firstName + " " + lastName));
        helper.setText(body, true);

        mailSender.send(message);

        System.out.print("Mail Sent");
    }

    public void sendLoginMail(String toEmail, String firstName, String lastName, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String body = "Gebruik deze code bij het inloggen op MB Ontdekt: "
                + "<br>"
                + "<h1>"
                + code
                + "</h1>"
                + "<br>"
                + "<br>"
                + "Met vriendelijke groet,"
                + "<br>"
                + " Ontdekstation 013"
                + "<br>"
                + "<img src=\"cid:logo.png\"></img><br/>";

        helper.setTo("kayletmail@host.com");
        helper.setTo(toEmail);
        helper.setFrom("local@gmail.com");
        helper.setSubject(String.format("Welkom %s", firstName + " " + lastName));
        helper.setText(body, true);

        mailSender.send(message);

        System.out.print("Mail Sent");
    }

    public void sendForgotPasswordMail(String toEmail, String firstName, String lastName, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String body = "<p> Beste " + firstName + " " + lastName + ", </p>"
                + "<p>Er is zojuist een verzoek gedaan om je wachtwoord te resetten. Om dit te doen kun je de volgende link openen:</p>"
                + "<a href=\"" + frontendHost + "/auth/reset-password?email=" + toEmail +"&token=" + code + "\">Wachtwoord resetten</a>"
                + "<p>Heb je dit verzoek niet gedaan? Dan kun je deze mail negeren.</p>"
                + "<p>Met vriendelijke groet,"
                + "<br>"
                + " Ontdekstation 013"
                + "<br>"
                + "<img src=\"cid:logo.png\"></img><br/></p>";

        helper.setTo("kayletmail@host.com");
        helper.setTo(toEmail);
        helper.setSubject("Wachtwoord resetten");
        helper.setText(body, true);

        mailSender.send(message);

        System.out.print("Mail Sent");
    }

    public void deleteUserMail(String toEmail, String firstName, String lastName) {
        SimpleMailMessage message = new SimpleMailMessage();
        String body = "we're extremely utterly insanely incredibly sorry to see you go :( sadface";
        message.setFrom("kayletmail@gmail.com");
        message.setTo(toEmail);
        message.setSubject(String.format("Welcome %s", firstName + " " + lastName));
        message.setText(String.format("Welcome %s", body));


        mailSender.send(message);
    }

    public void sendEmailEditMail(String toEmail, String firstName, String lastName, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String body = "Je hebt net je 013 email aangepast,"
                + "<br>"
                + "<br>"
                + "<br>"
                + code
                + "<br>"
                + "<br>"
                + "Met vriendelijke groet,"
                + "<br>"
                + " Ontdekstation 013"
                + "<br>"
                + "<img src=\"cid:logo.png\"></img><br/>";

        helper.setTo("kayletmail@host.com");
        helper.setTo(toEmail);
        helper.setSubject(String.format("Welkom %s", firstName + " " + lastName));
        helper.setText(body, true);

        mailSender.send(message);

        System.out.print("Mail Sent");
    }
}
