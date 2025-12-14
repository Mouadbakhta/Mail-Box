package org.example.mailbox;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailConfig {
    private final Properties properties = new Properties();

    public EmailConfig() {
        try (InputStream is = EmailConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            // ignore, use defaults
        }
        // override with environment variables if present
        overrideFromEnv("MAIL_SMTP_HOST", "mail.smtp.host");
        overrideFromEnv("MAIL_SMTP_PORT", "mail.smtp.port");
        overrideFromEnv("MAIL_SMTP_USER", "mail.smtp.user");
        overrideFromEnv("MAIL_SMTP_PASSWORD", "mail.smtp.password");
        overrideFromEnv("MAIL_SMTP_STARTTLS", "mail.smtp.starttls.enable");
        overrideFromEnv("MAIL_SMTP_SSL", "mail.smtp.ssl.enable");
        overrideFromEnv("MAIL_IMAP_HOST", "mail.imap.host");
        overrideFromEnv("MAIL_IMAP_PORT", "mail.imap.port");
        overrideFromEnv("MAIL_IMAP_USER", "mail.imap.user");
        overrideFromEnv("MAIL_IMAP_PASSWORD", "mail.imap.password");
        overrideFromEnv("MAIL_IMAP_SSL", "mail.imap.ssl.enable");
        overrideFromEnv("MAIL_PROTOCOL", "mail.protocol");
        overrideFromEnv("MAIL_MOCK_ENABLED", "mail.mock.enabled");
        overrideFromEnv("MAIL_FROM_ADDRESS", "mail.from.address");
    }

    private void overrideFromEnv(String envKey, String propKey) {
        String v = System.getenv(envKey);
        if (v != null && !v.isEmpty()) {
            properties.setProperty(propKey, v);
        }
    }

    public Properties getSmtpProperties() {
        Properties p = new Properties();
        p.put("mail.smtp.host", properties.getProperty("mail.smtp.host", "localhost"));
        p.put("mail.smtp.port", properties.getProperty("mail.smtp.port", "587"));
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", properties.getProperty("mail.smtp.starttls.enable", "true"));
        p.put("mail.smtp.ssl.enable", properties.getProperty("mail.smtp.ssl.enable", "false"));
        return p;
    }

    public Properties getImapProperties() {
        Properties p = new Properties();
        p.put("mail.store.protocol", properties.getProperty("mail.protocol", "imap"));
        p.put("mail.imap.host", properties.getProperty("mail.imap.host", "localhost"));
        p.put("mail.imap.port", properties.getProperty("mail.imap.port", "993"));
        p.put("mail.imap.ssl.enable", properties.getProperty("mail.imap.ssl.enable", "true"));
        return p;
    }

    public String getSmtpUser() { return properties.getProperty("mail.smtp.user", ""); }
    public String getSmtpPassword() { return properties.getProperty("mail.smtp.password", ""); }
    public String getImapUser() { return properties.getProperty("mail.imap.user", getSmtpUser()); }
    public String getImapPassword() { return properties.getProperty("mail.imap.password", getSmtpPassword()); }
    public boolean isMockEnabled() { return Boolean.parseBoolean(properties.getProperty("mail.mock.enabled", "true")); }
    public String getFromAddress() { return properties.getProperty("mail.from.address", getSmtpUser()); }
    public boolean isSmtpDebug() { return Boolean.parseBoolean(properties.getProperty("mail.smtp.debug", "false")); }
}
