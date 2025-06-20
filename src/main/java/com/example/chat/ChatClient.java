package com.example.chat;

import com.example.chat.db.DatabaseConnectionProvider;
import com.example.chat.db.DatabaseInitializer;
import com.example.chat.entity.Message;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.UserRepository;

import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatClient {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Use: java ChatClient <nick>");
            System.exit(1);
        }

        String username = args[0];
        try (Connection conn = DatabaseConnectionProvider.getConnection()) {
            DatabaseInitializer.init(conn);
            MessageRepository messageRepo = new MessageRepository(conn);
            UserRepository userRepo = new UserRepository(conn);

            try (ChatSender sender = new ChatSender(username, messageRepo)) {
                ChatReceiver receiver = new ChatReceiver(username, messageRepo);
                Thread receiverThread = new Thread(receiver);
                receiverThread.start();

                userRepo.markUserActive(username);
                userRepo.setStatus(username, "online");

                printChatHistory(messageRepo);
                printActiveUsers(userRepo);

                runChatLoop(username, sender, userRepo);

                receiver.stop();
                System.out.println("The chat has ended.");
            }
        }
    }

    private static void printChatHistory(MessageRepository repo) {
        System.out.println("📜 Latest Messages:\n");
        List<Message> history = repo.getLastMessages(10, false);
        for (int i = history.size() - 1; i >= 0; i--) {
            Message m = history.get(i);
            System.out.printf("[%s] %s: %s%n", m.createdAt().format(TIME_FORMAT), m.username(), m.content());
        }
    }

    private static void printActiveUsers(UserRepository repo) {
        Map<String, String> userStatuses = repo.getUserStatuses();
        System.out.println("\n👥 Online users:");
        userStatuses.forEach((name, status) -> {
            String color = getColorForStatus(status);
            System.out.println(" - " + color + name + "\u001B[0m");
        });
    }

    private static void runChatLoop(String username, ChatSender sender, UserRepository userRepo) {
        System.out.println("\nHello " + username + "! Write a message:");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();
            if (isExitCommand(input)) break;

            if (handleCommand(input, username, sender, userRepo)) continue;

            sender.sendMessage(input);
            userRepo.markUserActive(username);
        }
    }

    private static boolean isExitCommand(String input) {
        return "exit".equalsIgnoreCase(input) || "/exit".equalsIgnoreCase(input);
    }

    private static boolean handleCommand(String input, String username, ChatSender sender, UserRepository userRepo) {
        if (input.startsWith("/msg")) {
            String[] parts = input.trim().split("\\s+", 3);
            if (parts.length < 3) {
                System.out.println("❌ Incorrect command syntax '/msg'. Correct usage: /msg <nick> <tekst>");
                System.out.println("ℹ️  Type /help to see all available commands.");
            } else {
                String target = parts[1];
                String message = parts[2];
                sender.sendPrivateMessage(username, target, message);
                System.out.println("✉️  You -> " + target + ": " + message);
            }
            return true;
        }

        return switch (input) {
            case "/help" -> {
                printHelp();
                yield true;
            }
            case "/list" -> {
                printActiveUsers(userRepo);
                yield true;
            }
            case "/clear" -> {
                clearScreen();
                yield true;
            }
            case "/away", "/busy", "/online" -> {
                updateStatus(username, input.substring(1), userRepo);
                yield true;
            }
            default -> false;
        };
    }

    private static void printHelp() {
        System.out.println("""
            📘 Available commands:
            /msg <nick> <tekst> - private message
            /away, /busy, /online - change status
            /list - online users
            /clear - clean the screen
            /exit - exit from chat
        """);
    }

    private static void updateStatus(String username, String status, UserRepository userRepo) {
        userRepo.setStatus(username, status);
        System.out.println("✅ Status changed to: " + status);
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static String getColorForStatus(String status) {
        return switch (status.toLowerCase()) {
            case "online" -> "\u001B[32m"; // green
            case "away" -> "\u001B[33m"; // yellow
            case "busy" -> "\u001B[31m"; // red
            default -> "\u001B[0m";      // reset/default
        };
    }
}
