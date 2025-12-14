package org.example.mailbox.email;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.example.mailbox.EmailConfig;

import java.util.Properties;

public class SmtpEmailService implements EmailService {
    private final EmailConfig config;

    public SmtpEmailService(EmailConfig config) {
        this.config = config;
    }

    @Override
    public void sendEmail(String to, String subject, String body) throws Exception {
        Properties props = config.getSmtpProperties();
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getSmtpUser(), config.getSmtpPassword());
            }
        });
        session.setDebug(config.isSmtpDebug());

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.getFromAddress()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
    }

    @Override
    public java.util.List<org.example.mailbox.EmailItem> fetchInbox() { throw new UnsupportedOperationException("Use ImapEmailService for fetching"); }
    @Override
    public void delete(String id) { throw new UnsupportedOperationException("Use ImapEmailService for deletion"); }
    @Override
    public String fetchBody(String id) { throw new UnsupportedOperationException("Use ImapEmailService for body"); }
}
