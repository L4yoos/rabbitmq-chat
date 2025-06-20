package com.example.chat.db;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionProviderTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = DatabaseConnectionProvider.getConnection();
    }

    @Test
    void shouldEstablishConnection() throws Exception {
        assertNotNull(connection);
        assertFalse(connection.isClosed());
    }

    @Test
    void shouldExecuteSimpleQuery() throws Exception {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

