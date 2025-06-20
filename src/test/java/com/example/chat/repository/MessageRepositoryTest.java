package com.example.chat.repository;

import com.example.chat.entity.Message;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageRepositoryTest {

    private static Connection connection;
    private MessageRepository repository;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE messages (
                    id IDENTITY PRIMARY KEY,
                    username VARCHAR(100) NOT NULL,
                    content TEXT NOT NULL,
                    recipient VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
        }
    }

    @AfterAll
    static void closeDatabase() throws SQLException {
        if (connection != null) connection.close();
    }

    @BeforeEach
    void setup() {
        repository = new MessageRepository(connection);
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM messages");
        }
    }

    @Test
    void testRepositorySaveMessage() {
        repository.saveMessage("TestUser", "Hello!", "Alice");
        List<Message> messages = repository.getLastMessages(1, true); // ✅ includePrivate = true

        assertEquals(1, messages.size());
        assertEquals("TestUser", messages.get(0).username());
        assertEquals("Hello!", messages.get(0).content());
        assertEquals("Alice", messages.get(0).recipient());
    }

    @Test
    void testSaveAndRetrievePublicMessage() {
        repository.saveMessage("Alice", "Hello world!", null);
        List<Message> messages = repository.getLastMessages(10, false);

        assertEquals(1, messages.size());
        Message msg = messages.get(0);
        assertEquals("Alice", msg.username());
        assertEquals("Hello world!", msg.content());
        assertNotNull(msg.createdAt());
    }

    @Test
    void testSavePrivateMessage_notReturnedInPublicHistory() {
        repository.saveMessage("Bob", "Secret", "Charlie");
        List<Message> messages = repository.getLastMessages(10, false); // ✅ false -> public only

        assertTrue(messages.isEmpty(), "Private messages should not be returned in public history");
    }

    @Test
    void testMultipleMessagesAreOrderedByTimestamp() throws InterruptedException {
        repository.saveMessage("User1", "First", null);
        Thread.sleep(10); // ensure timestamp difference
        repository.saveMessage("User2", "Second", null);

        List<Message> messages = repository.getLastMessages(10, false);
        assertEquals(2, messages.size());
        assertEquals("User2", messages.get(0).username()); // newest first
        assertEquals("User1", messages.get(1).username());
    }

    @Test
    void testGetLastMessagesWithLimit() {
        for (int i = 1; i <= 5; i++) {
            repository.saveMessage("User", "Msg " + i, null);
        }

        List<Message> last3 = repository.getLastMessages(3, false);
        assertEquals(3, last3.size());
        assertEquals("Msg 5", last3.get(0).content()); // newest
        assertEquals("Msg 3", last3.get(2).content()); // oldest
    }
}
