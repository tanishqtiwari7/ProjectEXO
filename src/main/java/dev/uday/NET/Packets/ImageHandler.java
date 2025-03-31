package dev.uday.NET.Packets;

import dev.uday.Client;
import dev.uday.NET.Server;

import java.util.Arrays;
import java.util.UUID;

public class ImageHandler {
    public static void handleImagePacket(byte[] packetData, UUID sender) {
        byte msgType = packetData[0];
        byte[] imageData = Arrays.copyOfRange(packetData, 1, packetData.length);
        switch (msgType) {
            case 0:
                // Handle general image message
                handleGeneralImageMessage(imageData, sender);
                break;
            case 1:
                // Handle private image message
                handlePrivateImageMessage(imageData, sender);
                break;
        }
    }

    // Handle general image message
    private static void handleGeneralImageMessage(byte[] imageData, UUID sender) {
        byte packetType = 3;
        byte messageType = 0;
        String senderUsername = Server.currentClients.get(sender).username;
        byte[] imagePacket = new byte[imageData.length + 32];
        imagePacket[0] = packetType;
        imagePacket[1] = messageType;
        byte[] senderBytes = senderUsername.getBytes();
        System.arraycopy(senderBytes, 0, imagePacket, 2, senderBytes.length);
        // fill remaining bytes with 0
        for (int i = 2 + senderBytes.length; i < 32; i++) {
            imagePacket[i] = 0;
        }
        System.arraycopy(imageData, 0, imagePacket, 32, imageData.length);
        // Broadcast the image packet to all clients except the sender
        for (Client client : Server.currentClients.values()) {
            if (!client.clientHandler.uuid.equals(sender)) {
                client.clientHandler.sendPacket(imagePacket);
            }
        }
    }

    // Handle private image message
    private static void handlePrivateImageMessage(byte[] imageData, UUID sender) {
        byte[] usernameBytes = new byte[30];
        System.arraycopy(imageData, 0, usernameBytes, 0, 30);
        System.out.println("Username bytes: " + Arrays.toString(usernameBytes));
        String recipientUsername = new String(usernameBytes).trim();
        byte[] imageBytes = Arrays.copyOfRange(imageData, 30, imageData.length);
        byte packetType = 3;
        byte messageType = 1;
        byte[] imagePacket = new byte[imageBytes.length + 32];
        imagePacket[0] = packetType;
        imagePacket[1] = messageType;
        byte[] senderBytes = Server.currentClients.get(sender).username.getBytes();
        System.arraycopy(senderBytes, 0, imagePacket, 2, senderBytes.length);
        // fill remaining bytes with 0
        for (int i = 2 + senderBytes.length; i < 32; i++) {
            imagePacket[i] = 0;
        }

        System.arraycopy(imageBytes, 0, imagePacket, 32, imageBytes.length);
        // Send the image packet to the specific client

        UUID recipientUUID = Server.getUUIDFromUsername(recipientUsername);
        if (recipientUUID == null) {
            System.out.println("Recipient not found: " + recipientUsername );
            
        } else {
            Server.currentClients.get(recipientUUID).clientHandler.sendPacket(imagePacket);
        }
    }
}
