package com.example.chat.entity;

import java.time.LocalDateTime;

public class Message {
    private final long id;
    private final String username;
    private final String content;
    private final String recipient;
    private final LocalDateTime createdAt;

    public Message(long id, String username, String content, String recipient, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.content = content;
        this.recipient = recipient;
        this.createdAt = createdAt;
    }

    public long id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String content() {
        return content;
    }

    public String recipient() {
        return recipient;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        String base = "[" + createdAt + "] " + username;
        if (recipient != null) {
            base += " -> " + recipient;
        }
        return base + ": " + content;
    }
}
