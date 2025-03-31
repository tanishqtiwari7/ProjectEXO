package dev.uday.NET;

import java.io.IOException;
import java.net.*;

public class ServerBroadcasting implements Runnable {

    @Override
    public void run() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.setBroadcast(true);

            // Prepare broadcast data
            byte[] data = new byte[90];
            // 30 bytes for ip address, 30 bytes for port and 30 bytes for server name
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            String port = String.valueOf(Server.PORT);
            String serverName = Server.serverName;
            System.arraycopy(ipAddress.getBytes(), 0, data, 0, ipAddress.length());
            System.arraycopy(port.getBytes(), 0, data, 30, port.length());
            System.arraycopy(serverName.getBytes(), 0, data, 60, serverName.length());

            // Create broadcast address (255.255.255.255)
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");

            DatagramPacket broadcastPacket = new DatagramPacket(
                    data,
                    data.length,
                    broadcastAddress,
                    7415  // destination port
            );

            while (true) {
                datagramSocket.send(broadcastPacket);
                System.out.println("Broadcasting server information: " + ipAddress + ":" + port + " - " + serverName);
                // Wait for 2 seconds before sending the next broadcast
                Thread.sleep(2000);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}