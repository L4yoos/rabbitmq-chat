package com.example.chat;

import com.example.chat.db.DatabaseInitializer;
import com.example.chat.entity.Message;
import com.example.chat.repository.MessageRepository;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatSenderTest {

    private static Connection dbConnection;
    private MessageRepository messageRepository;
    private ChatSender chatSender;

    @BeforeAll
    static void setupDatabase() throws Exception {
        dbConnection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        DatabaseInitializer.init(dbConnection);
    }

    @AfterAll
    static void closeDb() throws Exception {
        dbConnection.close();
    }

    @BeforeEach
    void setUp() throws Exception {
        messageRepository = new MessageRepository(dbConnection);
        chatSender = new ChatSender("Alice", messageRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Statement stmt = dbConnection.createStatement()) {
            stmt.execute("DELETE FROM messages");
        }
        chatSender.close();
    }

    @Test
    void testSendMessage_savesPublicMessage() {
        chatSender.sendMessage("Hello, world!");

        List<Message> messages = messageRepository.getLastMessages(10, false);
        assertEquals(1, messages.size());

        Message message = messages.get(0);
        assertEquals("Alice", message.username());
        assertEquals("Hello, world!", message.content());
        assertNull(message.recipient());
    }

    @Test
    void testSendPrivateMessage_savesPrivateMessage() {
        chatSender.sendPrivateMessage("Bob", "Charlie", "Hi Charlie!");

        List<Message> messages = messageRepository.getLastMessages(10, true);
        assertEquals(1, messages.size());

        Message message = messages.get(0);
        assertEquals("Bob", message.username());
        assertEquals("Hi Charlie!", message.content());
        assertEquals("Charlie", message.recipient());
    }

    @Test
    void testSendPrivateMessage_notReturnedInPublicMessages() {
        chatSender.sendPrivateMessage("Alice", "Bob", "Secret");

        List<Message> publicMessages = messageRepository.getLastMessages(10, false);
        assertTrue(publicMessages.isEmpty(), "Private message should not appear in public history");

        List<Message> privateMessages = messageRepository.getLastMessages(10, true);
        assertEquals(1, privateMessages.size());
    }

    @Test
    void testMultipleMessagesOrder() throws InterruptedException {
        chatSender.sendMessage("First");
        Thread.sleep(10);
        chatSender.sendMessage("Second");

        List<Message> messages = messageRepository.getLastMessages(2, false);
        assertEquals("Second", messages.get(0).content());
        assertEquals("First", messages.get(1).content());
    }
}
