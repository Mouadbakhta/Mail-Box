package org.example.mailbox.email;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import org.example.mailbox.EmailConfig;
import org.example.mailbox.EmailItem;

import java.util.*;

public class ImapEmailService implements EmailService {
    private final EmailConfig config;

    public ImapEmailService(EmailConfig config) {
        this.config = config;
    }

    private Store connectStore() throws MessagingException {
        Properties props = config.getImapProperties();
        Session session = Session.getInstance(props);
        Store store = session.getStore(props.getProperty("mail.store.protocol", "imap"));
        store.connect(props.getProperty("mail.imap.host"), Integer.parseInt(props.getProperty("mail.imap.port", "993")), config.getImapUser(), config.getImapPassword());
        return store;
    }

    @Override
    public List<EmailItem> fetchInbox() throws Exception {
        try (Store store = connectStore()) {
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.getMessages();
            List<EmailItem> items = new ArrayList<>();
            for (Message m : messages) {
                String id = getMessageId(m);
                Address[] froms = m.getFrom();
                String from = (froms != null && froms.length > 0) ? froms[0].toString() : "";
                String subject = m.getSubject();
                Date date = m.getReceivedDate();
                String snippet = getTextFromMessage(m);
                if (snippet != null && snippet.length() > 120) snippet = snippet.substring(0, 120) + "...";
                items.add(new EmailItem(id, from, subject, date, snippet == null ? "" : snippet));
            }
            return items;
        }
    }

    @Override
    public void delete(String id) throws Exception {
        try (Store store = connectStore()) {
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            Message[] messages = inbox.getMessages();
            for (Message m : messages) {
                if (id.equals(getMessageId(m))) {
                    m.setFlag(Flags.Flag.DELETED, true);
                }
            }
            inbox.close(true); // expunge
        }
    }

    @Override
    public String fetchBody(String id) throws Exception {
        try (Store store = connectStore()) {
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.getMessages();
            for (Message m : messages) {
                if (id.equals(getMessageId(m))) {
                    return getTextFromMessage(m);
                }
            }
            return "";
        }
    }

    private String getMessageId(Message message) throws MessagingException {
        String[] headers = message.getHeader("Message-ID");
        return (headers != null && headers.length > 0) ? headers[0] : String.valueOf(message.getReceivedDate().getTime());
    }

    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return (String) message.getContent();
        } else if (message.isMimeType("text/html")) {
            return (String) message.getContent();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                // prefer plain text
                if (result.length() == 0) {
                    result.append(bodyPart.getContent());
                }
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    @Override
    public void sendEmail(String to, String subject, String body) throws Exception {
        throw new UnsupportedOperationException("Use SmtpEmailService for sending");
    }
}
