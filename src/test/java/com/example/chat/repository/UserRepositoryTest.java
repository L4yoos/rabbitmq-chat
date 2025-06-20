package com.example.chat.repository;

import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private static Connection connection;
    private UserRepository repository;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb_user;DB_CLOSE_DELAY=-1");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE active_users (
                    username VARCHAR(100) PRIMARY KEY,
                    last_seen TIMESTAMP NOT NULL,
                    status VARCHAR(20) DEFAULT 'online'
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
        repository = new UserRepository(connection);
    }

    @AfterEach
    void cleanup() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM active_users");
        }
    }

    @Test
    void testMarkUserActive_insertsNewUser() throws SQLException {
        repository.markUserActive("Alice");

        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM active_users WHERE username = 'Alice'")) {
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void testMarkUserActive_updatesLastSeenIfExists() throws InterruptedException, SQLException {
        repository.markUserActive("Bob");
        Thread.sleep(10);
        repository.markUserActive("Bob");

        Timestamp now = Timestamp.valueOf(java.time.LocalDateTime.now());
        try (PreparedStatement stmt = connection.prepareStatement("SELECT last_seen FROM active_users WHERE username = 'Bob'")) {
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            Timestamp lastSeen = rs.getTimestamp(1);
            assertTrue(Duration.between(lastSeen.toLocalDateTime(), now.toLocalDateTime()).toSeconds() < 5);
        }
    }

    @Test
    void testSetStatus_updatesStatusOnly() throws SQLException {
        repository.markUserActive("Charlie");
        repository.setStatus("Charlie", "away");

        try (PreparedStatement stmt = connection.prepareStatement("SELECT status FROM active_users WHERE username = 'Charlie'")) {
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("away", rs.getString(1));
        }
    }

    @Test
    void testUpdateStatus_updatesStatusAndLastSeen() throws SQLException, InterruptedException {
        repository.markUserActive("Dana");
        Thread.sleep(5);
        repository.updateStatus("Dana", "busy");

        try (PreparedStatement stmt = connection.prepareStatement("SELECT status, last_seen FROM active_users WHERE username = 'Dana'")) {
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("busy", rs.getString("status"));
            assertNotNull(rs.getTimestamp("last_seen"));
        }
    }

    @Test
    void testGetActiveUsers_returnsOnlyRecentlySeenUsers() throws Exception {
        repository.markUserActive("Eve");

        // wstaw ręcznie nieaktywnego użytkownika
        try (PreparedStatement stmt = connection.prepareStatement("""
            INSERT INTO active_users (username, last_seen, status)
            VALUES ('Frank', DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), 'away')
        """)) {
            stmt.executeUpdate();
        }

        List<String> active = repository.getActiveUsers();
        assertTrue(active.contains("Eve"));
        assertFalse(active.contains("Frank"));
    }

    @Test
    void testGetUserStatuses_returnsOnlyActiveWithStatuses() throws Exception {
        repository.markUserActive("Greg");
        repository.setStatus("Greg", "away");

        // wstaw nieaktywnego z innym statusem
        try (PreparedStatement stmt = connection.prepareStatement("""
            INSERT INTO active_users (username, last_seen, status)
            VALUES ('Hank', DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), 'busy')
        """)) {
            stmt.executeUpdate();
        }

        Map<String, String> statuses = repository.getUserStatuses();
        assertEquals(1, statuses.size());
        assertEquals("away", statuses.get("Greg"));
        assertNull(statuses.get("Hank"));
    }
}
