package com.example.chat;

import com.example.chat.repository.MessageRepository;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class ChatSender implements AutoCloseable {
    private static final String EXCHANGE_NAME = "chat-exchange";

    private final Connection connection;
    private final Channel channel;
    private final String username;
    private final MessageRepository messageRepository;

    public ChatSender(String username, MessageRepository messageRepository) throws IOException, TimeoutException {
        this.username = username;
        this.messageRepository = messageRepository;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");

        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
    }

    public void sendMessage(String content) {
        String formatted = username + ": " + content;
        publishMessage(formatted);
        messageRepository.saveMessage(username, content, null);
    }

    public void sendPrivateMessage(String sender, String recipient, String content) {
        String formatted = "[PM] " + sender + " -> " + recipient + ": " + content;
        publishMessage(formatted);
        messageRepository.saveMessage(sender, content, recipient);
    }

    private void publishMessage(String message) {
        try {
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("❌ Message publication error: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (channel != null && channel.isOpen()) channel.close();
            if (connection != null && connection.isOpen()) connection.close();
        } catch (IOException | TimeoutException e) {
            System.err.println("❌ Error closing ChatSender: " + e.getMessage());
        }
    }
}