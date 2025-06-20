package com.example.chat;

import com.example.chat.db.DatabaseConnectionProvider;
import com.example.chat.repository.MessageRepository;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.concurrent.TimeoutException;

public class ChatReceiver implements Runnable {

    private static final String EXCHANGE_NAME = "chat-exchange";
    private volatile boolean running = true;

    private Channel channel;
    private com.rabbitmq.client.Connection rabbitConnection;
    private Connection dbConnection;
    protected MessageRepository messageRepository;

    private final String currentUsername;

    public ChatReceiver(String currentUsername, MessageRepository repo) {
        this.currentUsername = currentUsername;
        this.messageRepository = repo;
    }

    @Override
    public void run() {
        try {
            setupConnections();
            startConsuming();

            while (running) {
                Thread.sleep(500);
            }

        } catch (Exception e) {
            System.err.println("❌ Receiver error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void setupConnections() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");

        rabbitConnection = factory.newConnection();
        channel = rabbitConnection.createChannel();

        dbConnection = DatabaseConnectionProvider.getConnection();
        messageRepository = new MessageRepository(dbConnection);
    }

    private void startConsuming() throws IOException {
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

        String queueName = "chat-queue-" + currentUsername;
        channel.queueDeclare(queueName, false, false, true, null);
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        DeliverCallback callback = (tag, delivery) -> {
            String rawMessage = new String(delivery.getBody(), StandardCharsets.UTF_8);
            processIncomingMessage(rawMessage);
        };

        channel.basicConsume(queueName, true, callback, tag -> {});
    }

    void processIncomingMessage(String rawMessage) {
        if (rawMessage.startsWith("[PM]")) {
            handlePrivateMessage(rawMessage.substring(5).trim());
        } else {
            handlePublicMessage(rawMessage);
        }
    }

    void handlePrivateMessage(String message) {
        String[] split = message.split("->", 2);
        if (split.length != 2) return;

        String sender = split[0].trim();
        String[] recipientParts = split[1].split(":", 2);
        if (recipientParts.length != 2) return;

        String recipient = recipientParts[0].trim();
        String content = recipientParts[1].trim();

        if (recipient.equalsIgnoreCase(currentUsername)) {
            System.out.println("\n✉️  " + sender + " (priv): " + content);
            System.out.print("> ");
        }
    }

    void handlePublicMessage(String message) {
        String[] parts = message.split(": ", 2);
        if (parts.length != 2) return;

        String sender = parts[0].trim();
        String content = parts[1].trim();

        if (!sender.equals(currentUsername)) {
            System.out.println("\n📩 " + sender + ": " + content);
        } else {
            System.out.println("\n📩 You: " + content);
        }

        System.out.print("> ");
    }

    private void cleanup() {
        try {
            if (channel != null && channel.isOpen()) channel.close();
            if (rabbitConnection != null && rabbitConnection.isOpen()) rabbitConnection.close();
            if (messageRepository != null) messageRepository.close();
        } catch (IOException | TimeoutException e) {
            System.err.println("❌ Resource cleaning error: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
    }
}
