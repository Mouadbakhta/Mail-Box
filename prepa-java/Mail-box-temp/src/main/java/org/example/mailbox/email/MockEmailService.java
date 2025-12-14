package org.example.mailbox.email;

import org.example.mailbox.EmailItem;

import java.util.*;

public class MockEmailService implements EmailService {
    private final List<EmailItem> items = new ArrayList<>();

    public MockEmailService() {
        items.add(new EmailItem("1", "alice@example.com", "Bienvenue", new Date(), "Bonjour et bienvenue dans votre boîte mail."));
        items.add(new EmailItem("2", "bob@example.com", "Rendez-vous", new Date(), "Peut-on caler un créneau demain ?"));
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        items.add(new EmailItem(UUID.randomUUID().toString(), to, subject, new Date(), body));
    }

    @Override
    public List<EmailItem> fetchInbox() { return new ArrayList<>(items); }

    @Override
    public void delete(String id) { items.removeIf(i -> i.getId().equals(id)); }

    @Override
    public String fetchBody(String id) {
        return items.stream().filter(i -> i.getId().equals(id)).map(EmailItem::getSnippet).findFirst().orElse("");
    }
}

