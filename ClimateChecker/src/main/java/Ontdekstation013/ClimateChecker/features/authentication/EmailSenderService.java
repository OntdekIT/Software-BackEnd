package Ontdekstation013.ClimateChecker.features.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailSenderService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String firstName, String lastName, String body){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("kayletmail@gmail.com");
        message.setTo(toEmail);
        message.setSubject(String.format("Welcome %s", firstName + " " + lastName));
        message.setText(String.format("Welcome %s", body));


        mailSender.send(message);

        System.out.printf("Mail Send");
    }

    public void sendSignupMail(String toEmail, String firstName, String lastName, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

//        File file = new File(".\\ClimateChecker\\src\\main\\resources\\images\\ontdekstation013-logo.png");
//        helper.addAttachment("logo.png", file);

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

        System.out.printf("Mail Sent");
    }

    public void sendLoginMail(String toEmail, String firstName, String lastName, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

//        File file = new File(".\\ClimateChecker\\src\\main\\resources\\images\\ontdekstation013-logo.png");
//        helper.addAttachment("logo.png", file);

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
        helper.setSubject(String.format("Welkom %s", firstName + " " + lastName));
        helper.setText(body, true);

        mailSender.send(message);

        System.out.printf("Mail Sent");
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

//        File file = new File(".\\ClimateChecker\\src\\main\\resources\\images\\ontdekstation013-logo.png");
//        helper.addAttachment("logo.png", file);

        String body = "Je hebt net je email changed 013,"
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

        System.out.printf("Mail Sent");
    }

}
