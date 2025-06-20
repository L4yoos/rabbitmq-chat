package com.example.chat.db;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void init(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS messages (
                    id SERIAL PRIMARY KEY,
                    username TEXT NOT NULL,
                    content TEXT NOT NULL,
                    recipient TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS active_users (
                    username TEXT PRIMARY KEY,
                    last_seen TIMESTAMP,
                    status TEXT DEFAULT 'online'
                )
            """);
        }
    }
}
