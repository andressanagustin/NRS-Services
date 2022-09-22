package com.test;

import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.Date;
import java.util.Properties;

public class EmailSender {

    private static Logger logger = Logger.getLogger(EmailSender.class);


    public static void sendMail(final String user, final String pass,
                                final String server,
                                final String mailFrom, final String mailTo,
                                final String subject, final String message) {
        try {

            System.setProperty("mail.mime.charset","UTF-8");

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

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(mailFrom));
            mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
            mimeMessage.setSubject(MimeUtility.encodeText(subject));
            mimeMessage.setSentDate(new Date());
            mimeMessage.setText(message);


            // Send email
            Transport.send(mimeMessage);

            logger.info("Mensaje enviado a : " + mailTo);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error enviando mensaje: " + e.getMessage());
        }


    }
}
