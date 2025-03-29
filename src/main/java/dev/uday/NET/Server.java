package dev.uday.NET;

import dev.uday.Client;
import dev.uday.Clients;
import dev.uday.NET.Packets.PacketHandler;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private ServerSocket serverSocket;
    private static final int PORT = 2005;
    public static ConcurrentHashMap<UUID, Client> currentClients = Clients.currentClients;
    private static KeyPair keyPair;

    public Server() {
        try {
            // Initialize server socket and generate key pair for secure communication
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            keyPair = generateKeyPair();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // Retrieves the UUID of a user if they are already logged in to prevent duplicate sessions
    public static UUID getUUIDFromUsername(String recipientUsername) {
        for (Client client : currentClients.values()) {
            if (client.username.equals(recipientUsername)) {
                return client.uuid; // Return the UUID if the user is found
            }
        }
        return null; // Return null if user is not found (not logged in)
    }

    public void start() {
        try {
            while (true) {
                // Accept new client connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

                // Read username from the client before assigning a UUID
                int length = inputStream.readInt();
                byte[] usernameBytes = new byte[length];
                inputStream.readFully(usernameBytes);
                String username = new String(usernameBytes);

                // Check if the user is already logged in
                UUID existingUUID = getUUIDFromUsername(username);

                if (existingUUID != null) {
                    // If user is already logged in, reject the new connection to avoid multiple active sessions
                    System.out.println("User already logged in: " + username);
                    clientSocket.close(); // Close the duplicate connection
                    continue; // Skip further processing for this client
                }

                // Generate a new UUID only if the user is not already in the system
                UUID uuid;
                while (true) {
                    uuid = UUID.randomUUID(); // Generate a random UUID
                    if (!currentClients.containsKey(uuid)) {
                        // Ensure UUID is unique before assigning it to the client
                        currentClients.put(uuid, new Client(clientSocket, uuid, username));
                        System.out.println("UUID assigned: " + uuid);
                        break;
                    }
                }

                // Create a new client handler thread for communication
                ClientHandler clientHandler = new ClientHandler(clientSocket, keyPair.getPublic(), uuid);
                currentClients.get(uuid).clientHandler = clientHandler;
                clientHandler.start(); // Start handling client communication
            }
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            stop(); // Ensure server stops gracefully
        }
    }

    // Generates a secure RSA key pair for encryption
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048); // Use 2048-bit encryption for security
        return keyGen.generateKeyPair();
    }

    public void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close(); // Close server socket when stopping
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
