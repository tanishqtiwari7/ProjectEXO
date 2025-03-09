package dev.uday.NET.Packets;

import java.util.Arrays;
import java.util.UUID;

public class PacketHandler {
    public static void handlePacket(byte[] packet, UUID sender) {
        byte packetType = packet[0];
        byte[] packetData = Arrays.copyOfRange(packet, 1, packet.length);
        System.out.println("Received packet of type " + packetType);
        switch (packetType) {
            //Handle message
            case 1:
                System.out.println("Handling message");
                MessageHandler.handleMessage(packetData, sender);
        }
    }
}
