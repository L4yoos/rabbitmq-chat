package com.example.chat.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionProvider {
    private static final String URL = "jdbc:postgresql://localhost:5432/chatdb";
    private static final String USER = "chatuser";
    private static final String PASSWORD = "chatpass";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
