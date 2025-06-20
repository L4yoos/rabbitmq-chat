package com.example.chat.repository;

import java.sql.*;
import java.util.*;

public class UserRepository implements AutoCloseable {
    private final Connection connection;

    public UserRepository(Connection connection) {
        this.connection = connection;
    }

    public void markUserActive(String username) {
        if (userExists(username)) {
            executeUpdate("UPDATE active_users SET last_seen = CURRENT_TIMESTAMP WHERE username = ?", username);
        } else {
            executeUpdate("INSERT INTO active_users (username, last_seen) VALUES (?, CURRENT_TIMESTAMP)", username);
        }
    }

    public List<String> getActiveUsers() {
        String sql = isPostgres()
                ? "SELECT username FROM active_users WHERE last_seen > NOW() - INTERVAL '2 minutes'"
                : "SELECT username FROM active_users WHERE last_seen > DATEADD('MINUTE', -2, CURRENT_TIMESTAMP)";
        return getUserList(sql);
    }

    public void setStatus(String username, String status) {
        executeUpdate("UPDATE active_users SET status = ? WHERE username = ?", status, username);
    }

    public void updateStatus(String username, String status) {
        String sql = isPostgres()
                ? "UPDATE active_users SET status = ?, last_seen = NOW() WHERE username = ?"
                : "UPDATE active_users SET status = ?, last_seen = CURRENT_TIMESTAMP WHERE username = ?";
        executeUpdate(sql, status, username);
    }

    public Map<String, String> getUserStatuses() {
        String sql = isPostgres()
                ? "SELECT username, status FROM active_users WHERE last_seen > NOW() - INTERVAL '5 minutes'"
                : "SELECT username, status FROM active_users WHERE last_seen > DATEADD('MINUTE', -5, CURRENT_TIMESTAMP)";

        Map<String, String> users = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.put(rs.getString("username"), rs.getString("status"));
            }
        } catch (SQLException e) {
            logError("downloading statuses", e);
        }
        return users;
    }

    private boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM active_users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logError("to check the existence of the user", e);
            return false;
        }
    }

    private void executeUpdate(String sql, String... params) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setString(i + 1, params[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            logError("data updates", e);
        }
    }

    private List<String> getUserList(String sql) {
        List<String> users = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            logError("user downloads", e);
        }
        return users;
    }

    private boolean isPostgres() {
        try {
            return connection.getMetaData().getURL().startsWith("jdbc:postgresql");
        } catch (SQLException e) {
            return false;
        }
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
