package com.sundar.devtech.Services;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
public class SendMail {
    private final String emailUsername = "sundar3112000@gmail.com";
    private final String emailPassword = "labo oxqy dcga fvoj";

    public void sendEmailWithAttachment(String recipientEmail, String subject, String bodyText) throws MessagingException {
        // SMTP server configuration
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // Session and authenticator for Gmail
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });

        // Compose email
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailUsername));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);

        // Add the email body text
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(bodyText);

        // Add the attachment
//        MimeBodyPart attachmentPart = new MimeBodyPart();
//        try {
//            attachmentPart.attachFile(attachment);
//            attachmentPart.setFileName(MimeUtility.encodeText("reports.pdf"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        // Multipart for body and attachment
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
//        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);

        // Send the message
        Transport.send(message);
    }
}
