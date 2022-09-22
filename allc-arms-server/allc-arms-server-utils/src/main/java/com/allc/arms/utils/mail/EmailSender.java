package com.allc.arms.utils.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

public class EmailSender {

    private static Logger logger = Logger.getLogger(EmailSender.class);

    public static void sendMail(final String user, final String pass,
            final String server,
            final String mailFrom, final String mailTo,
            final String subject, final String message) {
        try {

            System.setProperty("mail.mime.charset", "UTF-8");

            logger.info("Enviando mensaje a : " + mailTo);
            logger.info("Enviando mensaje a server: " + server);
            logger.info("Enviando mensaje a user: " + user);
            logger.info("Enviando mensaje a páss: " + pass);
            Properties properties = System.getProperties();

            properties.put("mail.smtp.host", server);
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(mailFrom));
            mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
            mimeMessage.setSubject(MimeUtility.encodeText(subject));
            mimeMessage.setSentDate(new Date());
            mimeMessage.setText(message);

            // Send email
            Transport.send(mimeMessage);

            logger.info("Mensaje enviado a : " + mailTo);

        } catch (UnsupportedEncodingException | MessagingException e) {
            logger.error("Error enviando mensaje: " + e.getMessage() , e);
        }

    }

    /**
     *
     * @param user
     * @param pass
     * @param server
     * @param mailFrom
     * @param mailTo
     * @param subject
     * @param messageText
     * @param filePath
     */
    public static void sendMailWithAttachment(final String user, final String pass,
            final String server,
            final String mailFrom, final String mailTo,
            final String subject, final String messageText, File filePath) {
        try {

            System.setProperty("mail.mime.charset", "UTF-8");

            logger.info("Enviando mensaje a : " + mailTo);
            Properties properties = System.getProperties();

            properties.put("mail.smtp.host", server);
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });

            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(mailFrom));

            // Set To: header field of the header.
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));

            // Set Subject: header field
            message.setSubject(MimeUtility.encodeText(subject));

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setContent(messageText,"text/html");

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();

            DataSource source = new FileDataSource(filePath);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filePath.getName());
            multipart.addBodyPart(messageBodyPart);

            // Send the complete message parts
            message.setContent(multipart);

            // Send message
            Transport.send(message);

            logger.info("Mensaje enviado a : " + mailTo);

        } catch (UnsupportedEncodingException e) {
            logger.error("Error enviando mensaje: " + e.getMessage());
        } catch (MessagingException e) {
            logger.error("Error enviando mensaje: " + e.getMessage());
        }

    }
}
