package com.example.chat;

import com.example.chat.db.DatabaseInitializer;
import com.example.chat.entity.Message;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatClientTest {

    private static Connection connection;
    private MessageRepository messageRepo;
    private UserRepository userRepo;

    @BeforeAll
    static void setupDatabase() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        DatabaseInitializer.init(connection);
    }

    @AfterAll
    static void closeDatabase() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @BeforeEach
    void setup() {
        messageRepo = new MessageRepository(connection);
        userRepo = new UserRepository(connection);
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM messages");
            stmt.executeUpdate("DELETE FROM active_users");
        }
    }

    @Test
    void testSendPublicMessage() {
        try (ChatSender sender = new ChatSender("Anna", messageRepo)) {
            sender.sendMessage("Hello world!");
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

        List<Message> messages = messageRepo.getLastMessages(5, false);
        assertFalse(messages.isEmpty());
        assertEquals("Hello world!", messages.get(0).content());
    }

    @Test
    void testPrivateMessage() throws Exception {
        try (ChatSender sender = new ChatSender("Anna", messageRepo)) {
            sender.sendPrivateMessage("Anna", "John", "Secret message");
        }

        List<Message> allMessages = getAllMessagesFromDb();
        boolean found = allMessages.stream()
                .anyMatch(msg -> "Secret message".equals(msg.content()) && "John".equals(msg.recipient()));
        assertTrue(found);
    }

    @Test
    void testMarkUserActiveAndStatus() {
        userRepo.markUserActive("Alice");
        userRepo.setStatus("Alice", "away");

        var statuses = userRepo.getUserStatuses();
        assertEquals("away", statuses.get("Alice"));
    }

    @Test
    void testUpdateStatusAlsoUpdatesLastSeen() {
        userRepo.markUserActive("Bob");
        userRepo.updateStatus("Bob", "busy");

        var statuses = userRepo.getUserStatuses();
        assertEquals("busy", statuses.get("Bob"));
    }

    private List<Message> getAllMessagesFromDb() throws SQLException {
        String sql = "SELECT id, username, content, recipient, created_at FROM messages";
        List<Message> messages = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                messages.add(new Message(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("content"),
                        rs.getString("recipient"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        }
        return messages;
    }
}
