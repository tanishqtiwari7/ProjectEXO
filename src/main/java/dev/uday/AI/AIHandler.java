package dev.uday.AI;

import dev.uday.NET.Server;

import java.util.UUID;

public class AIHandler {
    public static void handleAIPacket(byte[] packet, UUID uuid) {
        if (!Ollama.isOllamaAvailable) {
            Ollama.init();
        }
        byte packetType = packet[0];
        byte[] packetData = new byte[packet.length - 1];
        System.arraycopy(packet, 1, packetData, 0, packet.length - 1);
        String prompt = new String(packetData);
        System.out.println("Handling AI prompt: " + prompt);
        switch (packetType) {
            //Handle Text prompts
            case 1:
                System.out.println("Handling AI prompt");
                String response = Ollama.generate(uuid, prompt);
                sendResponse(uuid, response);
                break;
            default:
                System.out.println("Unknown AI packet type: " + packetType);
                break;
        }
    }

    private static void sendResponse(UUID uuid, String response) {
        byte AIPacket = 9;
        byte responseType = 1;
        byte[] responseData = response.getBytes();
        byte[] packet = new byte[responseData.length + 2];
        packet[0] = AIPacket;
        packet[1] = responseType;
        System.arraycopy(responseData, 0, packet, 2, responseData.length);
        // Send the packet back to the client
        Server.currentClients.get(uuid).clientHandler.sendPacket(packet);
        System.out.println("Sent AI response: " + response);
    }
}
