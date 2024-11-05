package com.sundar.devtech.Services;

import android.os.Environment;

import androidx.appcompat.app.AlertDialog;

import com.sundar.devtech.MainActivity;
import com.sundar.devtech.Masters.ReportActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class SendMail {

    private final String emailUsername = "sundar3112000@gmail.com";
    private final String appPassword = "labo oxqy dcga fvoj"; // App-specific password

    public Session serverConfiguration(){
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
                return new PasswordAuthentication(emailUsername, appPassword);
            }
        });

        return session;
    }

    public void sendEmailWithAttachment(String recipientEmail, String subject, String bodyText,int fileNum) throws MessagingException {

        // Compose email
        Message message = new MimeMessage(serverConfiguration());
        message.setFrom(new InternetAddress(emailUsername));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);

        // Add the email body text
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(bodyText);

        // Multipart for body and attachment
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);

        if (fileNum == 1) {
            // File attachment
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DEVTECH/" + "pentatvm_report.xlsx");
            MimeBodyPart attachmentPart = new MimeBodyPart();
            try {
                attachmentPart.attachFile(file);
                attachmentPart.setFileName(MimeUtility.encodeText("reports.xlsx"));
            } catch (IOException e) {
                throw new RuntimeException("Failed to attach file: " + e.getMessage(), e);
            }
            multipart.addBodyPart(attachmentPart); // Add the attachment part
        }

        message.setContent(multipart);

        // Send the message
        Transport.send(message);
    }
}
