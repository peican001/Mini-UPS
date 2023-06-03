package edu.duke.ece568.minUPS.service;

import edu.duke.ece568.minUPS.dao.PackageDao;
import edu.duke.ece568.minUPS.dao.UserDao;
import edu.duke.ece568.minUPS.entity.Package;
import edu.duke.ece568.minUPS.entity.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


@Service
public class EmailService {
    private static Logger LOG =  LoggerFactory.getLogger(EmailService.class);

    public void sendDeliveredEmail(long packageId ,String detail, String to){

        String subject = "UPS email";
        String body = "Your package " + packageId + ": " + detail + "has been delivered!";
        sendEmail(to,subject,body);
    }
    public void sendEmail(String to, String subject, String body) {
        // Set the email properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com"); // Replace with your SMTP server address
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Set your email credentials
        final String username = "chensuo568@gmail.com"; // Replace with your email address
        final String password = "bqjpfaqmbaunbhhg"; // Replace with your email password

        // Create a session with the email properties and credentials
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(username, password);
            }
        });

        try {
            // Create a message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            // Send the message
            Transport.send(message);

            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }



}
