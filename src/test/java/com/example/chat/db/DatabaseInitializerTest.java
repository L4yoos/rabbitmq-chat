package com.example.chat.db;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseInitializerTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = DatabaseConnectionProvider.getConnection();
        DatabaseInitializer.init(connection);
    }

    @Test
    void shouldCreateMessagesTable() throws Exception {
        assertTrue(tableExists("messages"), "Tabela 'messages' powinna istnieć");
    }

    @Test
    void shouldCreateActiveUsersTable() throws Exception {
        assertTrue(tableExists("active_users"), "Tabela 'active_users' powinna istnieć");
    }

    private boolean tableExists(String tableName) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet tables = metaData.getTables(null, null, tableName, null)) {
            return tables.next();
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (var stmt = connection.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS messages");
            stmt.executeUpdate("DROP TABLE IF EXISTS active_users");
        }
        if (!connection.isClosed()) {
            connection.close();
        }
    }
}

