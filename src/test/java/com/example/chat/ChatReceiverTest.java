package com.example.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ChatReceiverTest {

    private ChatReceiver receiver;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setup() {
        System.setOut(new PrintStream(outContent));
        receiver = new ChatReceiver("Marek", null);
    }

    @Test
    void testHandlePublicMessage_FromOtherUser_PrintsCorrectly() {
        receiver.handlePublicMessage("Konrad: Hello everyone!");

        String output = outContent.toString();
        assertTrue(output.contains("📩 Konrad: Hello everyone!"));
        assertTrue(output.contains(">"));
    }

    @Test
    void testHandlePublicMessage_FromSelf_PrintsCorrectly() {
        receiver.handlePublicMessage("Marek: It's me!");

        String output = outContent.toString();
        assertTrue(output.contains("📩 You: It's me!"));
        assertTrue(output.contains(">"));
    }

    @Test
    void testHandlePublicMessage_InvalidFormat_DoesNothing() {
        receiver.handlePublicMessage("Incorrect format");

        String output = outContent.toString();
        assertEquals("", output.trim());
    }

    @Test
    void testHandlePrivateMessage_ForCurrentUser_PrintsCorrectly() {
        receiver.handlePrivateMessage("Konrad -> Marek: Secret message");

        String output = outContent.toString();
        assertTrue(output.contains("✉️  Konrad (priv): Secret message"));
        assertTrue(output.contains(">"));
    }

    @Test
    void testHandlePrivateMessage_ForOtherUser_Ignored() {
        receiver.handlePrivateMessage("Konrad -> Faustyna: Hey!");

        String output = outContent.toString();
        assertEquals("", output.trim());
    }

    @Test
    void testHandlePrivateMessage_InvalidFormat_DoesNothing() {
        receiver.handlePrivateMessage("No separation");

        String output = outContent.toString();
        assertEquals("", output.trim());
    }

    @Test
    void testProcessIncomingMessage_Public() {
        receiver.processIncomingMessage("Konrad: Hi everyone!");

        String output = outContent.toString();
        assertTrue(output.contains("📩 Konrad: Hi everyone!"));
    }

    @Test
    void testProcessIncomingMessage_Private() {
        receiver.processIncomingMessage("[PM] Konrad -> Marek: Secret");

        String output = outContent.toString();
        assertTrue(output.contains("✉️  Konrad (priv): Secret"));
    }

    @Test
    void testProcessIncomingMessage_InvalidIgnored() {
        receiver.processIncomingMessage("Wrong message format");

        String output = outContent.toString();
        assertEquals("", output.trim());
    }
}
