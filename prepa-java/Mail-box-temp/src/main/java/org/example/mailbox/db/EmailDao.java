package org.example.mailbox.db;

import org.example.mailbox.EmailItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmailDao {
    private final DbConfig dbConfig;

    public EmailDao(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public void saveSent(String to, String subject, String body) throws SQLException {
        String sql = "INSERT INTO emails (direction, sender, recipient, subject, body, received_at) VALUES (?,?,?,?,?,NOW())";
        try (Connection c = dbConfig.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "SENT");
            ps.setString(2, null);
            ps.setString(3, to);
            ps.setString(4, subject);
            ps.setString(5, body);
            ps.executeUpdate();
        }
    }

    public void saveReceived(EmailItem item, String body) throws SQLException {
        String sql = "INSERT INTO emails (direction, external_id, sender, recipient, subject, body, received_at) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = dbConfig.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "RECEIVED");
            ps.setString(2, item.getId());
            ps.setString(3, item.getFrom());
            ps.setString(4, null);
            ps.setString(5, item.getSubject());
            ps.setString(6, body);
            ps.setTimestamp(7, new Timestamp(item.getDate() != null ? item.getDate().getTime() : System.currentTimeMillis()));
            ps.executeUpdate();
        }
    }

    public List<EmailItem> listAll() throws SQLException {
        String sql = "SELECT id, external_id, sender, subject, received_at, LEFT(body, 120) AS snippet FROM emails ORDER BY received_at DESC";
        try (Connection c = dbConfig.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<EmailItem> res = new ArrayList<>();
            while (rs.next()) {
                String id = rs.getString("external_id");
                String from = rs.getString("sender");
                String subject = rs.getString("subject");
                java.util.Date date = rs.getTimestamp("received_at");
                String snippet = rs.getString("snippet");
                res.add(new EmailItem(id != null ? id : ("db-" + rs.getLong("id")), from, subject, date, snippet));
            }
            return res;
        }
    }
}

