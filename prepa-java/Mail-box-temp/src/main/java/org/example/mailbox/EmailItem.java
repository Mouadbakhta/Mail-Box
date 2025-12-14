package org.example.mailbox;

import java.util.Date;

public class EmailItem {
    private final String id;
    private final String from;
    private final String subject;
    private final Date date;
    private final String snippet;

    public EmailItem(String id, String from, String subject, Date date, String snippet) {
        this.id = id;
        this.from = from;
        this.subject = subject;
        this.date = date;
        this.snippet = snippet;
    }

    public String getId() { return id; }
    public String getFrom() { return from; }
    public String getSubject() { return subject; }
    public Date getDate() { return date; }
    public String getSnippet() { return snippet; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s", from, subject, date);
    }
}

