package dev.uday.NET.Packets;

import dev.uday.NET.Server;

import java.util.Arrays;
import java.util.UUID;

public class MessageHandler {

    private static byte generalMessage = 0;
    private static byte privateMessage = 1;

    public static void handleMessage(byte[] packet, UUID sender) {
        System.out.println("Handling message 1");
        byte messageType = packet[0];
        byte[] messageData = Arrays.copyOfRange(packet, 1, packet.length);
        switch (messageType) {
            //Handle general message
            case 0:
                System.out.println("Handling general message");
                MessageHandler.handleGeneralMessage(messageData, sender);
                break;

            //Handle  private message
            case 1:
                System.out.println("Handling private message");
                MessageHandler.handlePrivateMessage(messageData, sender);
                break;
        }
    }

    private static void handleGeneralMessage(byte[] messageData, UUID sender) {
        // broadcast message to all clients
        byte packetType = 1;
        byte messageType = generalMessage;
        String senderUsername = Server.currentClients.get(sender).username;
        String message = new String(messageData);
        message = senderUsername + "x1W1x" + message;
        byte[] messagePacket = new byte[message.length() + 2];
        messagePacket[0] = packetType;
        messagePacket[1] = messageType;
        byte[] messageBytes = message.getBytes();
        System.arraycopy(messageBytes, 0, messagePacket, 2, messageBytes.length);
        Server.broadcast(messagePacket);
        System.out.println("Broadcasted message from " + senderUsername + ": " + message + " of type " + messageType + " with packet type " + packetType);
    }

    private static void handlePrivateMessage(byte[] messageData, UUID sender) {
        // send message to specific client
        String[] messageDataString = new String(messageData).split("x1W1x");
        String recipientUsername = messageDataString[0];
        String message = messageDataString[1];
        String senderUsername = Server.currentClients.get(sender).username;
        UUID recipient = Server.getUUIDFromUsername(recipientUsername);
        byte packetType = 1;
        byte messageType = privateMessage;
        message = senderUsername + "x1W1x" + message;
        byte[] messagePacket = new byte[message.length() + 2];
        messagePacket[0] = packetType;
        messagePacket[1] = messageType;
        byte[] messageBytes = message.getBytes();
        System.arraycopy(messageBytes, 0, messagePacket, 2, messageBytes.length);
        Server.currentClients.get(recipient).clientHandler.sendPacket(messagePacket);

        System.out.println("Sent private message from " + Server.currentClients.get(sender).username + " to " + recipientUsername + ": " + message);
    }
}
