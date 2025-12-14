package org.example.mailbox.db;

import org.example.mailbox.EmailConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConfig {
    private final String url;
    private final String user;
    private final String password;

    public DbConfig(EmailConfig emailConfig) {
        this.url = System.getenv().getOrDefault("JDBC_URL", emailConfigProperty(emailConfig, "jdbc.url", "jdbc:mysql://localhost:3306/mailbox?serverTimezone=UTC"));
        this.user = System.getenv().getOrDefault("JDBC_USER", emailConfigProperty(emailConfig, "jdbc.user", "root"));
        this.password = System.getenv().getOrDefault("JDBC_PASSWORD", emailConfigProperty(emailConfig, "jdbc.password", ""));
    }

    private String emailConfigProperty(EmailConfig cfg, String key, String def) {
        try {
            java.util.Properties p = new java.util.Properties();
            p.put(key, def);
            // EmailConfig ne expose pas getProperty directement; on relit via ressources déjà chargées dans la JVM
            // pour simplicité, on récupère depuis System properties si définies
            return System.getProperty(key, def);
        } catch (Exception e) {
            return def;
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

