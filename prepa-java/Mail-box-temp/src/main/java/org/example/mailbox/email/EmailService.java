package org.example.mailbox.email;

import org.example.mailbox.EmailItem;
import java.util.List;

public interface EmailService {
    void sendEmail(String to, String subject, String body) throws Exception;
    List<EmailItem> fetchInbox() throws Exception;
    void delete(String id) throws Exception;
    String fetchBody(String id) throws Exception;
}
