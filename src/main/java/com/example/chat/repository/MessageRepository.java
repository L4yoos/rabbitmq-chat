package com.example.chat.repository;

import com.example.chat.entity.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository implements AutoCloseable {
    private final Connection connection;

    public MessageRepository(Connection connection) {
        this.connection = connection;
    }

    public void saveMessage(String sender, String content, String recipient) {
        String sql = "INSERT INTO messages (username, content, recipient) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            prepareMessageStatement(stmt, sender, content, recipient);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logError("zapisu wiadomości", e);
        }
    }

    public List<Message> getLastMessages(int limit, boolean includePrivate) {
        String sql = includePrivate
                ? "SELECT id, username, content, recipient, created_at FROM messages ORDER BY created_at DESC LIMIT ?"
                : "SELECT id, username, content, recipient, created_at FROM messages WHERE recipient IS NULL ORDER BY created_at DESC LIMIT ?";

        List<Message> messages = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultToMessage(rs));
                }
            }
        } catch (SQLException e) {
            logError("pobierania wiadomości", e);
        }
        return messages;
    }

    private void prepareMessageStatement(PreparedStatement stmt, String sender, String content, String recipient) throws SQLException {
        stmt.setString(1, sender);
        stmt.setString(2, content);
        if (recipient != null) {
            stmt.setString(3, recipient);
        } else {
            stmt.setNull(3, Types.VARCHAR);
        }
    }

    private Message mapResultToMessage(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String username = rs.getString("username");
        String content = rs.getString("content");
        String recipient = rs.getString("recipient"); // ✅ dodane!
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
        return new Message(id, username, content, recipient, createdAt);
    }

    private void logError(String context, SQLException e) {
        System.err.printf("❌ Error %s: %s%n", context, e.getMessage());
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            logError("closing the connection", e);
        }
    }
}
